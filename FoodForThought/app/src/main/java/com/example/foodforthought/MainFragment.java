package com.example.foodforthought;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class MainFragment extends Fragment {

    private List<String> includeIngredients;


    /**
     * Class to store the number of matching queried ingredients a recipe has.
     */
    protected class Recipe{
        private String id;
        private Map<String, Object> recipe;
        private int matches;

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
            this.matches = 0;
            List<String> all_ingredients = (List<String>) recipe.get("all_ingredients");

            // For each ingredient in the recipe
            for(String ingredient : all_ingredients){

                // For each query ingredient
                for(String queryIngredient : toMatch){
                    if(ingredient.equals(queryIngredient)){
                        this.matches++;
                        break;
                    }
                }
            }
        }

        public String getId() {
            return id;
        }

        public Map<String, Object> getRecipe() {
            return recipe;
        }

        public int getMatches() {
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


        OnCompleteListener<QuerySnapshot> onGetRecipes = new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    StringBuilder res = new StringBuilder();
                    PriorityQueue<Recipe> matchingRecipes = new PriorityQueue<>(20, new Comparator<Recipe>() {
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
                    });

                    for(QueryDocumentSnapshot doc : task.getResult()){
                        matchingRecipes.offer(new Recipe(doc.getId(), doc.getData(), includeIngredients));
                    }
                    while(matchingRecipes.size() > 0){
                        Recipe currHighest = matchingRecipes.poll();
                        res.append(currHighest.getId()).append(" => ").append(currHighest.getRecipe().get("name")).append("\n");
                    }
                    recipeFeed.setText(res.toString());
                    System.out.println(res);
                }
                else{
                    Toast.makeText(getContext(),
                            "Error ! " + task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        };

        OnCompleteListener<DocumentSnapshot> onGetUserIngredients = new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    CollectionReference recipesRef = db.getDB().collection("recipes");
                    DocumentSnapshot userIngredients = task.getResult();
                    includeIngredients = (List<String>) userIngredients.get("inventory");
                    recipesRef.whereArrayContainsAny("all_ingredients", includeIngredients)
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

        db.getDocument("user_ingredients", userIngredientsId, onGetUserIngredients);

    }


}
