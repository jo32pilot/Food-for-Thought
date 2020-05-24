/**
 * The main page of the app. This fragment displays the user's recipe feed.
 *
 * @author John Li
 */
package com.example.foodforthought;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Class to display user's recipe feed.
 */
public class MainFragment extends Fragment {

    private List<String> userInventory;

    private static final int INIT_PQ_CAP = 20;

    /**
     * Class to store the number of matching queried ingredients a recipe has.
     */
    protected class Recipe{
        private String id;
        private Map<String, Object> recipe;
        private double matches;

        /**
         * Initializes number of queried ingredients that the recipe contains, as well as other
         * instance variables.
         * @param id Database Id of the recipe
         * @param recipe A mapping of the details of the recipe.
         * @param toMatch A list of the queried ingredients.
         */
        public Recipe(String id, Map<String, Object> recipe, List<String> toMatch){
            this.id = id;
            this.recipe = recipe;
            double countMatches = 0;

            // Stick all of the recipe's ingredients into a HashSet for O(1) lookup.
            HashSet<String> allIngredients =
                    new HashSet<>((List<String>) recipe.get("all_ingredients"));

            // For each query ingredient
            for(String queryIngredient : toMatch){
                // if in HashSet, then this recipe contains an ingredient in the user's inventory.
                if(allIngredients.contains(queryIngredient)){
                    countMatches++;
                }
            }

            // Compute ratio of num query ingredients in recipe to num ingredients in recipe.
            this.matches = countMatches / (double) allIngredients.size();

        }

        /**
         * Returns the recipe id.
         * @return the recipe id.
         */
        public String getId() {
            return id;
        }

        /**
         * Returns the recipe info.
         * @return the recipe info.
         */
        public Map<String, Object> getRecipe() {
            return recipe;
        }

        /**
         * The ratio of how many query ingredients are in the recipe.
         * @return the ratio of how many query ingredients are in the recipe.
         */
        public double getMatches() {
            return matches;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.test_recipe_feed, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        TextView recipeFeed = view.findViewById(R.id.testText);

        // If user isn't logged in or has logged out.
        if(user == null){
            // TODO redirect to login page
        }

        String uid = user.getUid();
        String userIngredientsId = "user_ingredients_id_" + uid;
        Database db = new Database();

        // Listener for when we've received recipes from the database.
        OnCompleteListener<QuerySnapshot> onGetRecipes = new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    StringBuilder res = new StringBuilder();

                    // Comparator for priority queue. If the ratio of user owned ingredients in the
                    // a recipe to the number of ingredients in that recipe is larger, then
                    // the recipe is "larger" / has higher priority.
                    Comparator<Recipe> recipeComparator = new Comparator<Recipe>() {
                        @Override
                        public int compare(Recipe o1, Recipe o2) {
                            if(o1.getMatches() > o2.getMatches()){
                                return -1;
                            }
                            else if(o1.getMatches() < o2.getMatches()){
                                return 1;
                            }
                            return 0;
                        }
                    };

                    PriorityQueue<Recipe> matchingRecipes =
                            new PriorityQueue<>(INIT_PQ_CAP, recipeComparator);

                    // Fill priority queue with recipes.
                    for(QueryDocumentSnapshot doc : task.getResult()){
                        matchingRecipes.offer(new Recipe(doc.getId(), doc.getData(), userInventory));
                    }

                    // While the queue isn't empty, keep popping recipes.
                    while(matchingRecipes.size() > 0){
                        Recipe currHighest = matchingRecipes.poll();

                        // For debugging / testing
                        System.out.println(currHighest.getMatches());

                        // For debugging / testing
                        res.append(currHighest.getId()).append(" => ")
                                .append(currHighest.getRecipe().get("name")).append("\n");
                    }
                    recipeFeed.setText(res.toString());

                }
                else{
                    Toast.makeText(getContext(),
                            "Error ! " + task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        };

        // Listener for when we've received the user's ingredients.
        OnCompleteListener<DocumentSnapshot> onGetUserIngredients = new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    CollectionReference recipesRef = db.getDB().collection("recipes");
                    DocumentSnapshot userIngredients = task.getResult();
                    userInventory = (List<String>) userIngredients.get("inventory");

                    // Begin second query to get recipes based on user's inventory.
                    recipesRef.whereArrayContainsAny("all_ingredients", userInventory)
                            .get()
                            .addOnCompleteListener(onGetRecipes);

                }
                else{
                    Toast.makeText(getContext(),
                            "Error ! " + task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        };

        // Where it all starts
        db.getDocument("user_ingredients", userIngredientsId, onGetUserIngredients);

    }


}
