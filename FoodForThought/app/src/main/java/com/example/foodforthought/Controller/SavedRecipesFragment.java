/**
 * File containing the functionality of the saved recipe page.
 *
 * @author Trevor Thomas
 */
package com.example.foodforthought.Controller;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodforthought.Model.Database;
import com.example.foodforthought.Model.Recipe;
import com.example.foodforthought.Model.RecipeAdapter;
import com.example.foodforthought.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * The saved recipe page fragment. This page displays a scrolling feed of the user's saved recipe.
 * It also has a button for adding homemade recipes to the database.
 */
public class SavedRecipesFragment extends Fragment {
    // view and variables
    private List<String> savedRecipes;
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
        // set default values
        view = inflater.inflate(R.layout.fragment_savedrecipes, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setHasFixedSize(true);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recipeAdapter = new RecipeAdapter(getContext(), new ArrayList<>(), false);
        recyclerView.setAdapter(recipeAdapter);
        recipeList = new ArrayList<>();

        // add new recipe button functionality
        TextView addRecipeButton = view.findViewById(R.id.addHomeMade);
        addRecipeButton.setOnClickListener(new View.OnClickListener() {
            /**
             * When the user clicks the add new recipe button, switch fragments.
             * @param view The button view.
             */
            @Override
            public void onClick(View view) {
                // switch fragments
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.container_fragment, new AddRecipeFragment());
                fragmentTransaction.commit();
            }
        });

        // get user_recipes information from the database
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        String userRecipesId = "user_recipes_id_" + uid;
        db.getDocument("user_recipes", userRecipesId, onGetUserRecipes);

        return view;
    }

    /**
     * Defaults out.
     * @param view Unused.
     * @param savedInstanceState Unused
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    // Listener for when we've received recipes from the database.
    OnCompleteListener<DocumentSnapshot> onGetRecipe = new OnCompleteListener<DocumentSnapshot>() {
        /**
         * When we get an individual recipe from the database, we add it to the recyclerView.
         * @param task Recipe info from database
         */
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();

                // add to scrolling feed
                recipeList.add(new Recipe(doc.getId(), doc.getData(), null));
                recipeAdapter = new RecipeAdapter(getContext(), recipeList, false);
                recyclerView.setAdapter(recipeAdapter);
            }
            else {
                // error logging
                Toast.makeText(getContext(),
                        "Error ! " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    // Listener for when we've received the user's ingredients.
    OnCompleteListener<DocumentSnapshot> onGetUserRecipes = new OnCompleteListener<DocumentSnapshot>() {
        /**
         * When we get the user_recipes from the database, we set up a recycler view of the
         * user's saved recipes.
         * @param task The user_recipes info
         */
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            if (task.isSuccessful()) {
                CollectionReference recipesRef = db.getDB().collection("recipes");
                DocumentSnapshot userRecipes = task.getResult();
                if (userRecipes != null) {
                    // Get user's inventory which is stored as a map (ingredient name -> amount)
                    savedRecipes = (ArrayList<String>) userRecipes.get("saved");
                    try {
                        // Begin second query to get recipes based on user's inventory.
                        if (savedRecipes != null) {
                            // get the info of every recipe in the saved list
                            for(int i = 0; i < savedRecipes.size(); i++) {
                                DocumentReference doc = recipesRef.document(savedRecipes.get(i));
                                doc.get().addOnCompleteListener(onGetRecipe);
                            }
                        }
                    }
                    catch(Exception e){
                        // error logging
                        Toast.makeText(getContext(),
                                "Error ! " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };
}