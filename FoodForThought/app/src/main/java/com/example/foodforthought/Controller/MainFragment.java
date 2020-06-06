/**
 * The main page of the app. This fragment displays the user's recipe feed.
 *
 * @author John Li
 */
package com.example.foodforthought.Controller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodforthought.Model.Database;
import com.example.foodforthought.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.example.foodforthought.Model.Recipe;
import com.example.foodforthought.Model.RecipeAdapter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Class to display user's recipe feed. What you see in the recipe feed is based on
 * what you have in you inventory. The more ingredients in the recipe that you have in
 * your inventory, the higher up that recipe will be in the feed.
 */
public class MainFragment extends Fragment {
    // constants
    private static final int INIT_PQ_CAP = 20;
    private static final int LIST_LIMIT = 50;

    // views and variables
    private List<String> userInventory;
    private RecyclerView recyclerView;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> recipeList;
    private Database db = new Database();
    private View view;

    /**
     * Builds the view when the fragment is opened.
     * @param inflater Inflated view to fit the screen.
     * @param container What the screen is contained in.
     * @param savedInstanceState Persists data throughout configuration changes.
     * @return The fully built view.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_main, container, false);

        // sets default values for all the views in the fragment
        recyclerView = view.findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setHasFixedSize(true);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recipeAdapter = new RecipeAdapter(getContext(), new ArrayList<>(), "fromMain");
        recyclerView.setAdapter(recipeAdapter);

        // If user isn't logged in or has logged out.
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            getActivity().finish();
        }
        else {
            // the user_ingredients id
            String uid = user.getUid();
            String userIngredientsId = "user_ingredients_id_" + uid;

            // Where it all starts
            db.getDocument("user_ingredients", userIngredientsId, onGetUserIngredients);
        }

        return view;
    }

    /**
     * Defaults out.
     * @param view Unused.
     * @param savedInstanceState Unused.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    // Listener for when we've received recipes from the database.
    OnCompleteListener<QuerySnapshot> onGetRecipes = new OnCompleteListener<QuerySnapshot>() {
        /**
         * Once we have gotten the list of recipes from the database, we only
         * want those with the ingredients we have available.
         * @param task Database information of recipes.
         */
        @Override
        public void onComplete(@NonNull Task<QuerySnapshot> task) {
            if (task.isSuccessful()) {
                // array list of recipes
                recipeList = new ArrayList<>();

                // Comparator for priority queue.
                Comparator<Recipe> recipeComparator = new Comparator<Recipe>() {
                    /**
                     * Comparator for priority queue. If the ratio of user owned ingredients in the
                     * a recipe to the number of ingredients in that recipe is larger, then
                     * the recipe is "larger" / has higher priority.
                     * @param o1 First recipe being compared
                     * @param o2 Second recipe being compared
                     * @return The higher priority recipe.
                     */
                    @Override
                    public int compare(Recipe o1, Recipe o2) {
                        if (o1.getMatches() < o2.getMatches()) {
                            return -1;
                        } else if (o1.getMatches() > o2.getMatches()) {
                            return 1;
                        }
                        return 0;
                    }
                };
                PriorityQueue<Recipe> matchingRecipes =
                        new PriorityQueue<>(INIT_PQ_CAP, recipeComparator);

                // Fill priority queue with recipes.
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    matchingRecipes.offer(new Recipe(doc.getId(), doc.getData(), userInventory));
                }

                // While the queue isn't empty, keep popping recipes.
                while (matchingRecipes.size() > 0) {
                    Recipe currHighest = matchingRecipes.poll();
                    recipeList.add(currHighest);
                }

                // set the adapter to the recycler view
                recipeAdapter = new RecipeAdapter(getContext(), recipeList, "fromMain");
                recyclerView.setAdapter(recipeAdapter);
            } else {
                // error logging
                Toast.makeText(getContext(),
                        "Error ! " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    // Listener for when we've received the user's ingredients.
    OnCompleteListener<DocumentSnapshot> onGetUserIngredients =
            new OnCompleteListener<DocumentSnapshot>() {
        /**
         * When we get the ingredients from the user inventory, we want to compare them to the
         * ingredients in the recipe feed.
         * @param task
         */
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            if (task.isSuccessful()) {
                CollectionReference recipesRef = db.getDB().collection("recipes");
                DocumentSnapshot userIngredients = task.getResult();
                if (userIngredients != null) {
                    // Get user's inventory which is stored as a map (ingredient name -> amount)
                    Map<String, Object> invMap =
                            (Map<String, Object>) userIngredients.get("inventory");
                    if(invMap == null){
                        userInventory = new ArrayList<>();
                    }
                    else{
                        // Get all ingredient names
                        userInventory = new ArrayList<String>(invMap.keySet());
                    }

                    // try to get the recipe which contain these ingredients
                    try {
                        // Begin second query to get recipes based on user's inventory.
                        if (userInventory != null) {
                            recipesRef.whereArrayContainsAny("all_ingredients", userInventory)
                                    .limit(LIST_LIMIT)
                                    .get()
                                    .addOnCompleteListener(onGetRecipes);
                        }
                    }
                    catch(Exception e){
                    }
                }
                else {
                    // go back to login page
                    NavHostFragment.findNavController(MainFragment.this)
                            .navigate(R.id.action_MainFragment_to_LoginFragment);
                }
            } else {
                // go back to login page
                NavHostFragment.findNavController(MainFragment.this)
                        .navigate(R.id.action_MainFragment_to_LoginFragment);
            }
        }
    };
}