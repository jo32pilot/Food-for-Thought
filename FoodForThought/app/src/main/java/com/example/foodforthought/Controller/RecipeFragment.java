/**
 * File that contains the functionality for building a recipe page from information in the
 * database.
 *
 * @author Trevor Thomas
 */
package com.example.foodforthought.Controller;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.foodforthought.Model.Database;
import com.example.foodforthought.Model.Recipe;
import com.example.foodforthought.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The fragment that displays recipe information. This fragment changes it child views based
 * on different recipes.
 */
public class RecipeFragment extends Fragment {
    // the database wrapper
    private Database db = new Database();

    // keeps track of what the user is doing
    private String userInput = "";
    private int userNumber = 0;

    /**
     * Builds the view when the fragment is opened.
     * @param inflater Inflated view to fit the screen.
     * @param container What the screen is contained in.
     * @param savedInstanceState Persists data throughout configuration changes.
     * @return The fully built view.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe, container, false);

        // If user isn't logged in or has logged out.
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser == null){
            getActivity().finish();
        }

        // deserialize recipe information
        Bundle bundle = getArguments();
        Recipe recipe = (Recipe) bundle.getSerializable("recipe");

        // color either like or dislike button
        OnCompleteListener<DocumentSnapshot> saveColor = new OnCompleteListener<DocumentSnapshot>() {
            /**
             * Colors the saved icon differently depending on if the user has
             * saved this recipe or not.
             * @param task The user's saved recipe list.
             */
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();

                    // list of saved recipes
                    ArrayList<String> savedRecipes = (ArrayList<String>) doc.get("saved");
                    for(int i = 0; i < savedRecipes.size(); i++) {
                        // if this recipe is in the saved list
                        if(recipe.getId().equals(savedRecipes.get(i))) {
                            // color like button
                            ImageButton save = view.findViewById(R.id.saveButton);
                            save.setImageResource(R.drawable.ic_saved);
                            return;
                        }
                    }
                }
                else {
                    // error logging
                    Toast.makeText(getContext(),
                            "Error ! " + task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        };
        db.getDocument("user_recipes", "user_recipes_id_"+firebaseUser.getUid(), saveColor);

        // save button functionality
        OnCompleteListener<DocumentSnapshot> onSave = new OnCompleteListener<DocumentSnapshot>() {
            /**
             * When the user clicks the save button, toggle save/unsave.
             * @param task Saved recipe list information.
             */
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task){
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();

                    // user's list of saved recipes
                    ArrayList<String> savedRecipes = (ArrayList<String>) doc.get("saved");

                    // if we have already saved this recipe
                    boolean inSaved = false;
                    for(int i = 0; i < savedRecipes.size(); i++) {
                        if(recipe.getId().equals(savedRecipes.get(i))) {
                            inSaved = true;
                        }
                    }

                    // if we have already saved this recipe
                    if (inSaved) {
                        // remove from saved
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("saved", FieldValue.arrayRemove(recipe.getId()));
                        db.update("user_recipes", "user_recipes_id_"+firebaseUser.getUid(),
                                map, RecipeFragment.this, "Deleted From Saved",
                                "Failed to delete from saved!");

                        // change the icon
                        ImageButton save = view.findViewById(R.id.saveButton);
                        save.setImageResource(R.drawable.ic_save);
                    }
                    else {
                        // add to saved
                        savedRecipes.add(recipe.getId());
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("saved", savedRecipes);
                        db.update("user_recipes", "user_recipes_id_"+firebaseUser.getUid(),
                                map, RecipeFragment.this, "Added to Saved",
                                "Failed to add to saved!");

                        // change the icon
                        ImageButton save = view.findViewById(R.id.saveButton);
                        save.setImageResource(R.drawable.ic_saved);
                    }
                }
            }
        };
        ImageButton saveButton = view.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Listen to user click of the save button. Accesses database after click.
             * @param v The save button view.
             */
            public void onClick(View v) {
                // add recipe to user's saved list
                db.getDocument("user_recipes", "user_recipes_id_"+firebaseUser.getUid(), onSave);
            }
        });

        // image
        ImageView recipeImage = view.findViewById(R.id.recipeImage);
        if(recipe.getURL() != "") {
            Picasso.with(getContext()).load(recipe.getURL()).into(recipeImage);
        }
        else {
            Picasso.with(getContext()).load("drawable://" + R.drawable.logo).into(recipeImage);
        }

        // likes and dislikes
        TextView likes = view.findViewById(R.id.numLikes);
        likes.setText(""+recipe.getLikes());
        TextView dislikes = view.findViewById(R.id.numDislikes);
        dislikes.setText(""+recipe.getDislikes());

        // color either like or dislike button
        OnCompleteListener<DocumentSnapshot> thumbColor = new OnCompleteListener<DocumentSnapshot>() {
            /**
             * Colors the like button text based on whether the user has like this recipe before
             * or not.
             * @param task The user's liked recipes list.
             */
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();

                    // if its in the like list
                    ArrayList<String> likedRecipes = (ArrayList<String>) doc.get("liked");
                    for(int i = 0; i < likedRecipes.size(); i++) {
                        if(recipe.getId().equals(likedRecipes.get(i))) {
                            // color like button
                            TextView likes = view.findViewById(R.id.numLikes);
                            likes.setTypeface(null, Typeface.BOLD);
                            return;
                        }
                    }

                    // if its in the disliked list
                    ArrayList<String> dislikedRecipes = (ArrayList<String>) doc.get("disliked");
                    for(int i = 0; i < dislikedRecipes.size(); i++) {
                        if(recipe.getId().equals(dislikedRecipes.get(i))) {
                            // color dislike button
                            TextView dislikes = view.findViewById(R.id.numDislikes);
                            dislikes.setTypeface(null, Typeface.BOLD);
                            return;
                        }
                    }
                }
                else {
                    // error logging
                    Toast.makeText(getContext(),
                            "Error ! " + task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        };
        db.getDocument("user_recipes", "user_recipes_id_"+firebaseUser.getUid(), thumbColor);

        // like action
        OnCompleteListener<DocumentSnapshot> onLike = new OnCompleteListener<DocumentSnapshot>() {
            /**
             * When the user likes a recipe, the databases is accessed.
             * Based on the database: if the recipe is already like, then do nothing.
             * If it was previously disliked, then undislike it and like it.
             * If it was neither, then just like it.
             * @param task Database info.
             */
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();

                    // checks if this recipe is in the liked list
                    boolean inLiked = false;
                    ArrayList<String> likedRecipes = (ArrayList<String>) doc.get("liked");
                    for(int i = 0; i < likedRecipes.size(); i++) {
                        if(recipe.getId().equals(likedRecipes.get(i))) {
                            inLiked = true;
                            break;
                        }
                    }

                    // checks if the recipe is in the disliked list
                    boolean inDisliked = false;
                    ArrayList<String> dislikedRecipes = (ArrayList<String>) doc.get("disliked");
                    for(int i = 0; i < dislikedRecipes.size(); i++) {
                        if(recipe.getId().equals(dislikedRecipes.get(i))) {
                            inDisliked = true;
                            break;
                        }
                    }

                    // if its been liked
                    if (inLiked) {
                        // do nothing
                        return;
                    }
                    // if its been disliked
                    else if (inDisliked) {
                        // remove from disliked, add to liked
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("disliked", FieldValue.arrayRemove(recipe.getId()));
                        db.update("user_recipes", "user_recipes_id_"+firebaseUser.getUid(),
                                map, RecipeFragment.this, "Deleted From Disliked",
                                "Failed to delete from disliked!");
                        ArrayList<String> addLiked = (ArrayList<String>)doc.get("liked");
                        addLiked.add(recipe.getId());
                        Map<String, Object> map2 = new HashMap<String, Object>();
                        map2.put("liked", addLiked);
                        db.update("user_recipes", "user_recipes_id_"+firebaseUser.getUid(),
                                map2, RecipeFragment.this, "Added to Liked",
                                "Failed to add to liked!");
                        long numDislikes = recipe.getDislikes();
                        numDislikes--;
                        recipe.setDislikes(numDislikes);
                        long numLikes = recipe.getLikes();
                        numLikes++;
                        recipe.setLikes(numLikes);
                        db.update("recipes", recipe.getId(), getRecipeMap(recipe, userInput),
                               RecipeFragment.this, "successfully liked recipe",
                               "failure to like recipe");
                    }
                    // if its neither been liked nor disliked
                    else {
                        // not liked or disliked
                        // add to liked
                        ArrayList<String> addLiked = (ArrayList<String>)doc.get("liked");
                        addLiked.add(recipe.getId());
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("liked", addLiked);
                        db.update("user_recipes", "user_recipes_id_"+firebaseUser.getUid(),
                                map, RecipeFragment.this, "Added to Liked",
                                "Failed to add to liked!");
                        long numLikes = recipe.getLikes();
                        numLikes++;
                        recipe.setLikes(numLikes);
                        db.update("recipes", recipe.getId(), getRecipeMap(recipe, userInput),
                               RecipeFragment.this, "successfully liked recipe",
                               "failure to like recipe");
                    }

                    // visual update
                    TextView likes = view.findViewById(R.id.numLikes);
                    likes.setText(""+recipe.getLikes());
                    likes.setTypeface(null, Typeface.BOLD);
                    TextView dislikes = view.findViewById(R.id.numDislikes);
                    dislikes.setText(""+recipe.getDislikes());
                    dislikes.setTypeface(null, Typeface.NORMAL);
                }
                else {
                    // error logging
                    Toast.makeText(getContext(),
                            "Error ! " + task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        };
        ImageButton likeButton = view.findViewById(R.id.likeButton);
        likeButton.setOnClickListener(new View.OnClickListener() {
            /**
             * When the like button is clicked, access the database.
             * @param view The view of the clicked button.
             */
            @Override
            public void onClick(View view) {
                db.getDocument("user_recipes", "user_recipes_id_"+firebaseUser.getUid(), onLike);
            }
        });

        // dislike action
        OnCompleteListener<DocumentSnapshot> onDislike = new OnCompleteListener<DocumentSnapshot>() {
            /**
             * When the user dislikes a recipe, the databases is accessed.
             * Based on the database: if the recipe is already disliked, then do nothing.
             * If it was previously liked, then unlike it and dislike it.
             * If it was neither, then just dislike it.
             * @param task Database info.
             */
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();

                    // if it is in the liked list
                    boolean inLiked = false;
                    ArrayList<String> likedRecipes = (ArrayList<String>) doc.get("liked");
                    for(int i = 0; i < likedRecipes.size(); i++) {
                        if(recipe.getId().equals(likedRecipes.get(i))) {
                            inLiked = true;
                            break;
                        }
                    }

                    // if it is in the disliked list
                    boolean inDisliked = false;
                    ArrayList<String> dislikedRecipes = (ArrayList<String>) doc.get("disliked");
                    for(int i = 0; i < dislikedRecipes.size(); i++) {
                        if(recipe.getId().equals(dislikedRecipes.get(i))) {
                            inDisliked = true;
                            break;
                        }
                    }

                    // if it was previously disliked
                    if (inDisliked) {
                        // do nothing
                        return;
                    }
                    // if it was previously liked
                    else if (inLiked) {
                        // remove from liked, add to disliked
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("liked", FieldValue.arrayRemove(recipe.getId()));
                        db.update("user_recipes", "user_recipes_id_"+firebaseUser.getUid(),
                                map, RecipeFragment.this, "Deleted From Liked",
                                "Failed to delete from Liked!");
                        ArrayList<String> addDisliked = (ArrayList<String>)doc.get("disliked");
                        addDisliked.add(recipe.getId());
                        Map<String, Object> map2 = new HashMap<String, Object>();
                        map2.put("disliked", addDisliked);
                        db.update("user_recipes", "user_recipes_id_"+firebaseUser.getUid(),
                                map2, RecipeFragment.this, "Added to Disliked",
                                "Failed to add to disliked!");
                        long numDislikes = recipe.getDislikes();
                        numDislikes++;
                        recipe.setDislikes(numDislikes);
                        long numLikes = recipe.getLikes();
                        numLikes--;
                        recipe.setLikes(numLikes);
                        db.update("recipes", recipe.getId(), getRecipeMap(recipe, userInput),
                                RecipeFragment.this, "successfully disliked recipe",
                                "failure to dislike recipe");
                    }
                    // if neither
                    else {
                        // not liked or disliked
                        // add to liked
                        ArrayList<String> addDisliked = (ArrayList<String>)doc.get("disliked");
                        addDisliked.add(recipe.getId());
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("disliked", addDisliked);
                        db.update("user_recipes", "user_recipes_id_"+firebaseUser.getUid(),
                                map, RecipeFragment.this, "Added to Disliked",
                                "Failed to add to disliked!");
                        long numDislikes = recipe.getDislikes();
                        numDislikes++;
                        recipe.setDislikes(numDislikes);
                        db.update("recipes", recipe.getId(), getRecipeMap(recipe, userInput),
                                RecipeFragment.this, "successfully disliked recipe",
                                "failure to dislike recipe");
                    }

                    // visual update
                    TextView likes = view.findViewById(R.id.numLikes);
                    likes.setText(""+recipe.getLikes());
                    likes.setTypeface(null, Typeface.NORMAL);
                    TextView dislikes = view.findViewById(R.id.numDislikes);
                    dislikes.setText(""+recipe.getDislikes());
                    dislikes.setTypeface(null, Typeface.BOLD);
                }
                else {
                    // error logging
                    Toast.makeText(getContext(),
                            "Error ! " + task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        };
        ImageButton dislikeButton = view.findViewById(R.id.dislikeButton);
        dislikeButton.setOnClickListener(new View.OnClickListener() {
            /**
             * When the dislike button is clicked, access the database.
             * @param view The view of the clicked button.
             */
            @Override
            public void onClick(View view) {
                db.getDocument("user_recipes", "user_recipes_id_"+firebaseUser.getUid(), onDislike);
            }
        });

        // recipe name
        TextView recipeName = view.findViewById(R.id.recipeName);
        recipeName.setText(recipe.getName());

        // author field
        TextView author = view.findViewById(R.id.recipeAuthor);
        if(recipe.getAuthor().equals("")) {
            author.setText("By: Food For Thought");
        }
        else {
            author.setText("By: " + recipe.getAuthor());
        }

        // servings
        TextView servingSize = view.findViewById(R.id.servingSize);
        servingSize.setText(recipe.getYield());

        // total time
        TextView totalTime = view.findViewById(R.id.totalTime);
        totalTime.setText("" + recipe.getTime() + " minutes");

        // ingredients
        OnCompleteListener<DocumentSnapshot> onGetUserInventory= new OnCompleteListener<DocumentSnapshot>() {
            /**
             * When the page is loaded, we must determine what ingredients the user has
             * in their inventory.
             * @param task The user inventory data.
             */
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();

                    // if we have all the ingredients, we will display the make button
                    boolean hasAllIngredients = true;

                    // data structures from the database
                    LinearLayout recipeIngredients = view.findViewById(R.id.recipeIngredients);
                    ArrayList<Map<String, Object>> ingredients = recipe.getIngredients();
                    Map<String, Object> inventory = (Map<String, Object>) doc.get("inventory");

                    // go through each ingredient in the recipe
                    for(int i = 0; i < ingredients.size(); i++) {
                        // create a new checkbox
                        CheckBox checkBox = new CheckBox(view.getContext());
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        checkBox.setLayoutParams(params);
                        checkBox.setText((String)ingredients.get(i).get("ingredient"));
                        checkBox.setEnabled(false);

                        // if this ingredient is saved as a string in the database
                        if(ingredients.get(i).get("parsed_ingredient") instanceof String) {
                            // if we have this ingredient in our inventory
                            if (inventory.containsKey(ingredients.get(i).get("parsed_ingredient"))) {
                                // check it off
                                checkBox.setChecked(true);
                            }
                            else {
                                // we don't have every ingredient
                                hasAllIngredients = false;
                            }
                        }
                        // this ingredient is saved as an array in the database, ie it has multiple
                        // parsed ingredients
                        else {
                            // its an array of parsed ingredients
                            ArrayList<String> parsed = (ArrayList<String>)ingredients.get(i).get("parsed_ingredient");
                            boolean hasIngredients = true;
                            for (int j = 0; j < parsed.size(); j++) {
                                if(!inventory.containsKey(parsed.get(j)))
                                    hasIngredients = false;
                            }
                            if(hasIngredients)
                                checkBox.setChecked(true);
                            else
                                hasAllIngredients = false;
                        }
                        recipeIngredients.addView(checkBox);
                    }

                    // add "Make" button
                    if(hasAllIngredients) {
                        Button button = new Button(getContext());
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(300, 150);
                        button.setLayoutParams(params);
                        button.setGravity(Gravity.CENTER);
                        button.setText("MAKE");
                        button.setOnClickListener(new View.OnClickListener() {
                            /**
                             * When the make button is clicked, remove the used ingredients from
                             * the user's inventory.
                             * @param v The make button view.
                             */
                            @Override
                            public void onClick(View v) {
                                // get rid of used inventory
                                for(int i = 0; i < ingredients.size(); i ++) {
                                    // it is a single ingredient
                                    if(ingredients.get(i).get("parsed_ingredient") instanceof String) {
                                        String parsedIngredient = (String)ingredients.get(i).get("parsed_ingredient");

                                        // the amount of the ingredient we have left
                                        long num;
                                        if(inventory.get(parsedIngredient) instanceof String) {
                                            num = Long.parseLong((String)inventory.get(parsedIngredient));
                                        }
                                        else {
                                            // its a long
                                            num = (long)inventory.get(parsedIngredient);
                                        }

                                        // if we have the ingredient
                                        if (inventory.containsKey(parsedIngredient) &&
                                            num > 0) {
                                            // we now have one less of the ingredient
                                            num--;

                                            // if this has set the ingredient amount to 0
                                            if(num == 0) {
                                                // we must remove it
                                                inventory.remove(parsedIngredient);
                                            }
                                            else {
                                                // otherwise, simply update it
                                                inventory.put(parsedIngredient, num);
                                            }
                                        }
                                    }
                                    // its an array of parsed ingredients
                                    else {
                                        ArrayList<String> parsedIngredients = (ArrayList<String>)ingredients.get(i).get("parsed_ingredient");

                                        // get rid of each of the parsed ingredients
                                        for(int j = 0; j < parsedIngredients.size(); j++) {
                                            String thisIngredient = parsedIngredients.get(j);
                                            long num;
                                            if(inventory.get(thisIngredient) instanceof String) {
                                                num = Long.parseLong((String)inventory.get(thisIngredient));
                                            }
                                            else {
                                                // its a long
                                                num = (long)inventory.get(thisIngredient);
                                            }
                                            if (inventory.containsKey(thisIngredient) &&
                                                    num > 0) {
                                                num--;

                                                if(num == 0) {
                                                    inventory.remove(thisIngredient);
                                                }
                                                else {
                                                    inventory.put(thisIngredient, num);
                                                }
                                            }
                                        }
                                    }
                                }

                                // now inventory has updated values
                                // need to push changes to database
                                Map<String, Object> hash = new HashMap<>();
                                hash.put("inventory", inventory);
                                db.update("user_ingredients", "user_ingredients_id_"+firebaseUser.getUid(),
                                        hash, RecipeFragment.this, "Updated Inventory",
                                        "Failed to Update Inventory");

                                // reload page
                                userInput = "";
                                userNumber = 0;
                                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                if (Build.VERSION.SDK_INT >= 26) {
                                    ft.setReorderingAllowed(false);
                                }
                                ft.detach(RecipeFragment.this).attach(RecipeFragment.this).commit();
                            }
                        });

                        // add the make button to the page
                        LinearLayout buttonHolder = view.findViewById(R.id.makeButtonHolder);
                        buttonHolder.addView(button);
                    }
                }
                else {
                    // error logging
                    Toast.makeText(getContext(),
                            "Error ! " + task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        };
        // check if the ingredients we have in our inventory
        db.getDocument("user_ingredients", "user_ingredients_id_"+firebaseUser.getUid(),
                onGetUserInventory);

        // instructions
        LinearLayout recipeInstructions = view.findViewById(R.id.recipeInstructions);
        ArrayList<String> instructions = recipe.getInstructions();
        for(int i = 0; i < instructions.size(); i++) {
            // null check
            if(instructions.get(i) == "") {
                continue;
            }

            // instruction holder
            LinearLayout instruction = new LinearLayout(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            params.setMargins(0,0,0,10);
            instruction.setOrientation(LinearLayout.HORIZONTAL);
            instruction.setGravity(Gravity.CENTER_VERTICAL);
            instruction.setLayoutParams(params);

            // "Step #)"
            TextView step = new TextView(getContext());
            LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            step.setLayoutParams(params2);
            step.setText("Step " + (i+1) + ")");
            step.setTextColor(Color.parseColor("#FD999A"));

            // the information of this step
            TextView infoText = new TextView(getContext());
            LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params3.setMargins(10,0,0,0);
            infoText.setLayoutParams(params3);
            infoText.setGravity(Gravity.LEFT);
            infoText.setText(instructions.get(i));

            // add it to the page
            instruction.addView(step);
            instruction.addView(infoText);
            recipeInstructions.addView(instruction);
        }

        // load comments section
        LinearLayout commentsSection = view.findViewById(R.id.recipeComments);
        OnCompleteListener<DocumentSnapshot> onGetUser = new OnCompleteListener<DocumentSnapshot>() {
            /**
             * When the user who posted the comment is gotten from the database, load their profile
             * picture and username to the page.
             * @param task The user data.
             */
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();

                    // create user profile image
                    ImageView userProfile = new ImageView(view.getContext());
                    userProfile.setImageResource(R.drawable.profilepic);
                    LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(150, 150);
                    userProfile.setLayoutParams(imageParams);

                    // name of user field
                    TextView originalPoster = new TextView(getContext());
                    originalPoster.setTypeface(null, Typeface.BOLD);
                    if(doc.exists()){
                        // create username
                        originalPoster.setText((String)doc.get("username"));

                        // load default picture
                        if (doc.get("profilePictureURL") != null) {
                            Picasso.with(getContext()).load((String)doc.get("profilePictureURL")).into(userProfile);
                        }
                    }
                    else {
                        // use undefined user name
                        originalPoster.setText("unknown user");
                    }

                    // create new comment
                    LinearLayout newComment = new LinearLayout(view.getContext());
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.MATCH_PARENT);
                    params.setMargins(0, 0, 0, 10);
                    newComment.setLayoutParams(params);

                    // div text section
                    LinearLayout textSection = new LinearLayout(getContext());
                    LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    params2.setMargins(10, 0, 0, 0);
                    textSection.setLayoutParams(params2);
                    textSection.setOrientation(LinearLayout.VERTICAL);

                    // create user comment text
                    TextView userText = new TextView((view.getContext()));
                    LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    userText.setLayoutParams(textParams);
                    userText.setText(recipe.getComments().get(userNumber).get("comment"));

                    // add user comment text and user profile to the comment
                    newComment.addView(userProfile);
                    textSection.addView(originalPoster);
                    textSection.addView(userText);
                    newComment.addView(textSection);

                    // add the comment to the comment section
                    commentsSection.addView(newComment);

                    // another comment has been added
                    userNumber++;

                    // set the number of comments on the page
                    TextView numComments = view.findViewById(R.id.numComments);
                    numComments.setText("" + userNumber);
                }
                else {
                    // error logging
                    Toast.makeText(getContext(),
                            "Error ! " + task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        };
        for (int i = 0; i < recipe.getComments().size(); i++) {
            db.getDocument("users", recipe.getComments().get(i).get("userid"), onGetUser);
        }

        // back button
        ImageButton backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Switches fragments when the back button is clicked.
             * @param v The view of the back button.
             */
            public void onClick(View v) {
                // get ready to switch fragments
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                // depending on what page we came from. go back to a different page
                if(bundle.getBoolean("fromMain") == true) {
                    fragmentTransaction.replace(R.id.container_fragment, new MainFragment());
                }
                else {
                    fragmentTransaction.replace(R.id.container_fragment, new SavedRecipesFragment());
                }
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        // comment feature
        ImageButton addCommentButton = view.findViewById(R.id.addCommentButton);
        addCommentButton.setOnClickListener(new View.OnClickListener() {
            /**
             * When the add comment button is pressed, make a popup.
             * @param v The add comment button view.
             */
            public void onClick(View v) {
                // alert box to collect user input
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Add Comment:");

                // Set up the input
                final EditText input = new EditText(view.getContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    /**
                     * When the user presses OK, then post the comment.
                     * @param dialog The popup box
                     * @param which Unused
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // get user input
                        userInput = input.getText().toString();
                        db.update("recipes", recipe.getId(), getRecipeMap(recipe, userInput),
                                RecipeFragment.this, "successfully added comment",
                                "failure to add comment");
                        db.getDocument("users", firebaseUser.getUid(), onGetUser);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    /**
                     * When the user presses Cancel, get rid of the popup
                     * @param dialog The popup box.
                     * @param which Unused
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                // show the alert dialog box
                builder.show();
            }
        });

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

    /**
     * Constructs a HashMap containing recipe information. Used to update the database.
     * @param r The recipe
     * @param comment An addtional comment
     * @return The constructed map object.
     */
    private Map<String, Object> getRecipeMap(Recipe r, String comment){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // null check
        if(comment != "") {
            // add the new comment to the map
            Map<String, String> hash = new HashMap<>();
            hash.put("comment", comment);
            hash.put("userid", user.getUid());
            r.getComments().add(hash);
        }

        // add the redundant information to the map
        Map<String, Object> recipe = new HashMap<>();
        recipe.put("all_ingredients", r.getAllIngredients());
        recipe.put("image", r.getURL());
        recipe.put("ingredients", r.getIngredients());
        recipe.put("instructions", r.getInstructions());
        recipe.put("name", r.getName());
        recipe.put("total_time", r.getTime());
        recipe.put("user_created", r.getAuthor());
        recipe.put("yield", r.getYield());
        recipe.put("comments", r.getComments());
        recipe.put("likes", r.getLikes());
        recipe.put("dislikes", r.getDislikes());

        return recipe;
    }
}