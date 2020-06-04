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

// Fragment to load the user profile fragment
public class UserProfileFragment extends Fragment {
    private TextView name;
    private Database db = new Database();
    private View view;
    private String user_name;
    private List<String> customRecipes;

    private RecyclerView recyclerView;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> recipeList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_userprofile, container, false);
        recyclerView = view.findViewById(R.id.recycler_view3);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setHasFixedSize(true);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recipeAdapter = new RecipeAdapter(getContext(), new ArrayList<>(), false);
        recyclerView.setAdapter(recipeAdapter);
        recipeList = new ArrayList<>();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            getActivity().finish();
        }

        String uid = user.getUid();
        name = view.findViewById(R.id.username);

        db.getDocument("users", uid, onGetName);
        String userRecipesId = "user_recipes_id_" + uid;
        db.getDocument("user_recipes", userRecipesId, onGetUserRecipes);

        view.findViewById(R.id.LogoutButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                getActivity().finish();
            }
        });

        return view;
    }

    OnCompleteListener<DocumentSnapshot> onGetName = new OnCompleteListener<DocumentSnapshot>() {
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    // Listener for when we've received recipes from the database.
    OnCompleteListener<DocumentSnapshot> onGetRecipe = new OnCompleteListener<DocumentSnapshot>() {
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                recipeList.add(new Recipe(doc.getId(), doc.getData(), null));
                recipeAdapter = new RecipeAdapter(getContext(), recipeList, false);
                recyclerView.setAdapter(recipeAdapter);
            }
            else {
                Toast.makeText(getContext(),
                        "Error ! " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    // Listener for when we've received the user's ingredients.
    OnCompleteListener<DocumentSnapshot> onGetUserRecipes = new OnCompleteListener<DocumentSnapshot>() {
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

                            for(int i = 0; i < customRecipes.size(); i++) {
                                DocumentReference doc = recipesRef.document(customRecipes.get(i));
                                doc.get().addOnCompleteListener(onGetRecipe);
                            }
                        }
                    }
                    catch(Exception e){
                    }
                }
            }
        }
    };

}
