/**
 * The main page of the app. This fragment displays the user's recipe feed.
 *
 * @author John Li
 */
package com.example.foodforthought;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodforthought.Misc.Database;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.example.foodforthought.Misc.Recipe;
import com.example.foodforthought.Misc.RecipeAdapter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Class to display user's recipe feed.
 */
public class MainFragment extends Fragment {

    private List<String> userInventory;
    private List<String> images;
    private RecyclerView recyclerView;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> recipeList;
    private List<String> followingList;
    private Database db = new Database();

    private View view;

    private static final int INIT_PQ_CAP = 20;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_main, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setHasFixedSize(true);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recipeAdapter = new RecipeAdapter(getContext(), new ArrayList<>(), true);
        recyclerView.setAdapter(recipeAdapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        TextView recipeFeed = view.findViewById(R.id.testText);

        // If user isn't logged in or has logged out.
        if (user == null) {
            getActivity().finish();
        }
        else {
            String uid = user.getUid();
            String userIngredientsId = "user_ingredients_id_" + uid;

            // Where it all starts
            db.getDocument("user_ingredients", userIngredientsId, onGetUserIngredients);
        }
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    // Listener for when we've received recipes from the database.
    OnCompleteListener<QuerySnapshot> onGetRecipes = new OnCompleteListener<QuerySnapshot>() {
        @Override
        public void onComplete(@NonNull Task<QuerySnapshot> task) {
            if (task.isSuccessful()) {
                StringBuilder res = new StringBuilder();
                recipeList = new ArrayList<>();

                // Comparator for priority queue. If the ratio of user owned ingredients in the
                // a recipe to the number of ingredients in that recipe is larger, then
                // the recipe is "larger" / has higher priority.
                Comparator<Recipe> recipeComparator = new Comparator<Recipe>() {
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

                    // For debugging / testing
//                    System.out.println(currHighest.getMatches());
                    // For debugging / testing
//                    res.append(currHighest.getId()).append(" => ")
//                            .append(currHighest.getRecipe().get("name")).append("\n");
                }
//                    recipeFeed.setText(res.toString());
                recipeAdapter = new RecipeAdapter(getContext(), recipeList, true);
                recyclerView.setAdapter(recipeAdapter);
            } else {
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
            if (task.isSuccessful()) {
                CollectionReference recipesRef = db.getDB().collection("recipes");
                DocumentSnapshot userIngredients = task.getResult();
                if (userIngredients != null) {

                    // Get user's inventory which is stored as a map (ingredient name -> amount)
                    Map<String, Object> invMap = (Map<String, Object>) userIngredients.get("inventory");

                    if(invMap == null){
                        userInventory = new ArrayList<>();
                    }
                    else{
                        // Get all ingredient names
                        userInventory = new ArrayList<String>(invMap.keySet());
                    }


                    try {
                        // Begin second query to get recipes based on user's inventory.
                        if (userInventory != null) {
                            recipesRef.whereArrayContainsAny("all_ingredients", userInventory)
                                    .limit(25)
                                    .get()
                                    .addOnCompleteListener(onGetRecipes);
                        }
                    }
                    catch(Exception e){
                    }
                }
                else {

                    NavHostFragment.findNavController(MainFragment.this)
                            .navigate(R.id.action_MainFragment_to_LoginFragment);
                }
            } else {
                NavHostFragment.findNavController(MainFragment.this)
                        .navigate(R.id.action_MainFragment_to_LoginFragment);
            }
        }
    };

}
