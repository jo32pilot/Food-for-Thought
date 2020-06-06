/**
 * The file containing the functionality of the profile page.
 *
 * @author Luz Acevedo
 */
package com.example.foodforthought.Controller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
 * Class to display user's profile page. The profile page has the user's username, a
 * logout button, and a feed of their created recipes.
 */
public class UserProfileFragment extends Fragment {
    // views and variables
    private TextView name;
    private Database db = new Database();
    private View view;
    private String user_name;
    private List<String> customRecipes;
    private RecyclerView recyclerView;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> recipeList;

    /**
     * Builds the view when the fragment is opened.
     * @param inflater Inflated view to fit the screen.
     * @param container What the screen is contained in.
     * @param savedInstanceState Persists data throughout configuration changes.
     * @return The fully built view.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // default initializations
        view = inflater.inflate(R.layout.fragment_userprofile, container, false);
        recyclerView = view.findViewById(R.id.recycler_view3);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setHasFixedSize(true);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recipeAdapter = new RecipeAdapter(getContext(), new ArrayList<>(), "fromProfile");
        recyclerView.setAdapter(recipeAdapter);
        recipeList = new ArrayList<>();

        // If user isn't logged in or has logged out.
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            getActivity().finish();
        }

        // the user id used to access the database
        String uid = user.getUid();
        name = view.findViewById(R.id.username);

        // get database information
        db.getDocument("users", uid, onGetName);
        String userRecipesId = "user_recipes_id_" + uid;
        db.getDocument("user_recipes", userRecipesId, onGetUserRecipes);

        // when the logout button is clicked
        view.findViewById(R.id.LogoutButton).setOnClickListener(new View.OnClickListener() {
            /**
             * Signs out the user and goes back to the login page.
             * @param v The view of the logout button.
             */
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                getActivity().finish();
            }
        });

        return view;
    }

    // when the username is retrieved
    OnCompleteListener<DocumentSnapshot> onGetName = new OnCompleteListener<DocumentSnapshot>() {
        /**
         * Displays the username after it is gotten from the database.
         * @param task The user info.
         */
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            if (task.isSuccessful()) {
                DocumentSnapshot user_profile = task.getResult();
                user_name = (String) user_profile.get("username");
                if (user_name != null) {
                    name.setText(user_name);
                }
            }

        }
    };

    /**
     * Defualts out
     * @param view Unused
     * @param savedInstanceState Unused
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    // Listener for when we've received recipes from the database.
    OnCompleteListener<DocumentSnapshot> onGetRecipe = new OnCompleteListener<DocumentSnapshot>() {
        /**
         * Creates a scroll feed of selfmade recipes on the page.
         * @param task The recipe list info.
         */
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                recipeList.add(new Recipe(doc.getId(), doc.getData(), null));
                recipeAdapter = new RecipeAdapter(getContext(), recipeList, "fromProfile");
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

    // Listener for when we've received the user's recipes
    OnCompleteListener<DocumentSnapshot> onGetUserRecipes =
            new OnCompleteListener<DocumentSnapshot>() {
        /**
         * Once the self made recipes are received, get the individuals recipe data.
         * @param task The self_made recipes list.
         */
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            if (task.isSuccessful()) {
                CollectionReference recipesRef = db.getDB().collection("recipes");
                DocumentSnapshot userRecipes = task.getResult();
                if (userRecipes != null) {
                    // Get user's inventory which is stored as a map (ingredient name -> amount)
                    customRecipes = (ArrayList<String>) userRecipes.get("self_made");

                    try {
                        // Begin second query to get recipes based on user's inventory.
                        if (customRecipes != null) {
                            // get info of each individual recipe in the list
                            for(int i = 0; i < customRecipes.size(); i++) {
                                DocumentReference doc = recipesRef.document(customRecipes.get(i));
                                doc.get().addOnCompleteListener(onGetRecipe);
                            }
                        }
                    }
                    catch(Exception e) {
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