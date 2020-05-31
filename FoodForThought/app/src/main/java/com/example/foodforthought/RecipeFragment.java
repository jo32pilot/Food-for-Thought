package com.example.foodforthought;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
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
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.example.foodforthought.Misc.Database;
import com.example.foodforthought.Misc.Recipe;
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

public class RecipeFragment extends Fragment {

    private Database db = new Database();
    private String userInput = "";
    private int userNumber = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe, container, false);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // deserialize recipe information
        Bundle bundle = getArguments();
        Recipe recipe = (Recipe) bundle.getSerializable("recipe");

        // page title
        TextView recipePageTitle = view.findViewById(R.id.recipePageTitle);
        recipePageTitle.setText(recipe.getName());

        // color either like or dislike button
        OnCompleteListener<DocumentSnapshot> saveColor = new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();

                    ArrayList<String> savedRecipes = (ArrayList<String>) doc.get("saved");
                    for(int i = 0; i < savedRecipes.size(); i++) {
                        if(recipe.getId().equals(savedRecipes.get(i))) {
                            // color like button
                            ImageButton save = view.findViewById(R.id.saveButton);
                            save.setImageResource(R.drawable.ic_saved);
                            return;
                        }
                    }
                }
                else {
                    Toast.makeText(getContext(),
                            "Error ! " + task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        };
        db.getDocument("user_recipes", "user_recipes_id_"+firebaseUser.getUid(), saveColor);

        // save button functionality
        OnCompleteListener<DocumentSnapshot> onSave = new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task){
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    ArrayList<String> savedRecipes = (ArrayList<String>) doc.get("saved");

                    boolean inSaved = false;
                    for(int i = 0; i < savedRecipes.size(); i++) {
                        if(recipe.getId().equals(savedRecipes.get(i))) {
                            inSaved = true;
                        }
                    }

                    if (inSaved) {
                        // remove from saved
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("saved", FieldValue.arrayRemove(recipe.getId()));
                        db.update("user_recipes", "user_recipes_id_"+firebaseUser.getUid(),
                                map, RecipeFragment.this, "Deleted From Saved",
                                "Failed to delete from saved!");

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

                        ImageButton save = view.findViewById(R.id.saveButton);
                        save.setImageResource(R.drawable.ic_saved);
                    }
                }
            }
        };
        ImageButton saveButton = view.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
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
        else{
            Picasso.with(getContext()).load("drawable://" + R.drawable.logo).into(recipeImage);
        }

        // likes and dislikes
        TextView likes = view.findViewById(R.id.numLikes);
        likes.setText(""+recipe.getLikes());
        TextView dislikes = view.findViewById(R.id.numDislikes);
        dislikes.setText(""+recipe.getDislikes());

        // color either like or dislike button
        OnCompleteListener<DocumentSnapshot> thumbColor = new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();

                    ArrayList<String> likedRecipes = (ArrayList<String>) doc.get("liked");
                    for(int i = 0; i < likedRecipes.size(); i++) {
                        if(recipe.getId().equals(likedRecipes.get(i))) {
                            // color like button
                            TextView likes = view.findViewById(R.id.numLikes);
                            likes.setTypeface(null, Typeface.BOLD);
                            return;
                        }
                    }

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
                    Toast.makeText(getContext(),
                            "Error ! " + task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        };
        db.getDocument("user_recipes", "user_recipes_id_"+firebaseUser.getUid(), thumbColor);


        // like action
        OnCompleteListener<DocumentSnapshot> onLike = new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    DocumentSnapshot doc = task.getResult();

                    boolean inLiked = false;
                    ArrayList<String> likedRecipes = (ArrayList<String>) doc.get("liked");
                    for(int i = 0; i < likedRecipes.size(); i++) {
                        if(recipe.getId().equals(likedRecipes.get(i))) {
                            inLiked = true;
                            break;
                        }
                    }

                    boolean inDisliked = false;
                    ArrayList<String> dislikedRecipes = (ArrayList<String>) doc.get("disliked");
                    for(int i = 0; i < dislikedRecipes.size(); i++) {
                        if(recipe.getId().equals(dislikedRecipes.get(i))) {
                            inDisliked = true;
                            break;
                        }
                    }

                    if (inLiked) {
                        // do nothing
                        return;
                    }
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
                    Toast.makeText(getContext(),
                            "Error ! " + task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        };
        ImageButton likeButton = view.findViewById(R.id.likeButton);
        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                db.getDocument("user_recipes", "user_recipes_id_"+firebaseUser.getUid(), onLike);
            }
        });

        // dislike action
        OnCompleteListener<DocumentSnapshot> onDislike = new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    DocumentSnapshot doc = task.getResult();

                    boolean inLiked = false;
                    ArrayList<String> likedRecipes = (ArrayList<String>) doc.get("liked");
                    for(int i = 0; i < likedRecipes.size(); i++) {
                        if(recipe.getId().equals(likedRecipes.get(i))) {
                            inLiked = true;
                            break;
                        }
                    }

                    boolean inDisliked = false;
                    ArrayList<String> dislikedRecipes = (ArrayList<String>) doc.get("disliked");
                    for(int i = 0; i < dislikedRecipes.size(); i++) {
                        if(recipe.getId().equals(dislikedRecipes.get(i))) {
                            inDisliked = true;
                            break;
                        }
                    }

                    if (inDisliked) {
                        // do nothing
                        return;
                    }
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
                    Toast.makeText(getContext(),
                            "Error ! " + task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        };
        ImageButton dislikeButton = view.findViewById(R.id.dislikeButton);
        dislikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
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
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();

                    boolean hasAllIngredients = true;

                    LinearLayout recipeIngredients = view.findViewById(R.id.recipeIngredients);
                    ArrayList<Map<String, Object>> ingredients = recipe.getIngredients();
                    Map<String, Object> inventory = (Map<String, Object>) doc.get("inventory");
                    for(int i = 0; i < ingredients.size(); i++) {
                        // create a new checkbox
                        CheckBox checkBox = new CheckBox(view.getContext());
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        checkBox.setLayoutParams(params);
                        checkBox.setText((String)ingredients.get(i).get("ingredient"));

                        checkBox.setEnabled(false);

                        if(ingredients.get(i).get("parsed_ingredient") instanceof String) {
                            if (inventory.containsKey(ingredients.get(i).get("parsed_ingredient")))
                                checkBox.setChecked(true);
                            else
                                hasAllIngredients = false;
                        }
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
                            @Override
                            public void onClick(View v) {
                                // get rid of used inventory
                                for(int i = 0; i < ingredients.size(); i ++) {
                                    if(ingredients.get(i).get("parsed_ingredient") instanceof String) {
                                        // it is a single ingredient
                                        String parsedIngredient = (String)ingredients.get(i).get("parsed_ingredient");

                                        long num;
                                        if(inventory.get(parsedIngredient) instanceof String) {
                                            num = Long.parseLong((String)inventory.get(parsedIngredient));
                                        }
                                        else {
                                            // its a long
                                            num = (long)inventory.get(parsedIngredient);
                                        }
                                        if (inventory.containsKey(parsedIngredient) &&
                                            num > 0) {
                                            num--;

                                            if(num == 0) {
                                                inventory.remove(parsedIngredient);
                                            }
                                            else {
                                                inventory.put(parsedIngredient, num);
                                            }
                                        }
                                    }
                                    else {
                                        // its an array of parsed ingredients
                                        ArrayList<String> parsedIngredients = (ArrayList<String>)ingredients.get(i).get("parsed_ingredient");
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

                        LinearLayout buttonHolder = view.findViewById(R.id.makeButtonHolder);
                        buttonHolder.addView(button);
                    }
                }
                else {
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
            if(instructions.get(i) == "") {
                continue;
            }

            LinearLayout instruction = new LinearLayout(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            params.setMargins(0,0,0,10);
            instruction.setOrientation(LinearLayout.HORIZONTAL);
            instruction.setGravity(Gravity.CENTER_VERTICAL);
            instruction.setLayoutParams(params);

            TextView step = new TextView(getContext());
            LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            step.setLayoutParams(params2);
            step.setText("Step " + (i+1) + ")");
            step.setTextColor(Color.parseColor("#FD999A"));

            TextView infoText = new TextView(getContext());
            LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params3.setMargins(10,0,0,0);
            infoText.setLayoutParams(params3);
            infoText.setGravity(Gravity.LEFT);
            infoText.setText(instructions.get(i));

            instruction.addView(step);
            instruction.addView(infoText);

            recipeInstructions.addView(instruction);
        }

        // load comments section
        LinearLayout commentsSection = view.findViewById(R.id.recipeComments);

        // callback for getting user
        OnCompleteListener<DocumentSnapshot> onGetUser = new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();

                    // create user profile
                    ImageView userProfile = new ImageView(view.getContext());
                    userProfile.setImageResource(R.drawable.profilepic);
                    LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(150, 150);
                    userProfile.setLayoutParams(imageParams);

                    TextView originalPoster = new TextView(getContext());
                    originalPoster.setTypeface(null, Typeface.BOLD);
                    if(doc.exists()){
                        // create username
                        originalPoster.setText((String)doc.get("username"));

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

                    TextView numComments = view.findViewById(R.id.numComments);
                    numComments.setText("" + userNumber);
                }
                else{
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
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
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
            private long numberOfComments = recipe.getComments().size();
            public void onClick(View v) {
                //
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Add Comment:");

                // Set up the input
                final EditText input = new EditText(view.getContext());
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        // get user input
                        userInput = input.getText().toString();
                        db.update("recipes", recipe.getId(), getRecipeMap(recipe, userInput),
                                RecipeFragment.this, "successfully added comment",
                                "failure to add comment");
                        db.getDocument("users", firebaseUser.getUid(), onGetUser);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private Map<String, Object> getRecipeMap(Recipe r, String comment){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(comment != "") {
            Map<String, String> hash = new HashMap<>();
            hash.put("comment", comment);
            hash.put("userid", user.getUid());
            r.getComments().add(hash);
        }

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
