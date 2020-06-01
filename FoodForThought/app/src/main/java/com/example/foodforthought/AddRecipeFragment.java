/**
 * File holds functionality for allowing users to add their own recipes.
 * @author John Li
 */

package com.example.foodforthought;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.foodforthought.Misc.AddListAdapter;
import com.example.foodforthought.Misc.CustomAutoCompleteTextChangedListener;
import com.example.foodforthought.Misc.CustomAutoCompleteView;
import com.example.foodforthought.Misc.Database;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Fragment for add recipe functionality. This object is only ever created once
 * in the home activity so that its state persists upon leaving this fragment.
 */
public class AddRecipeFragment extends Fragment {

    ArrayList<String> ingredients = new ArrayList<>();
    ArrayList<String> instructions = new ArrayList<>();
    EditText recipeName, instruction, time, servings;

    // Custom auto completion to suggest ingredients in our database.
    AutoCompleteTextView ingredient;
    ArrayAdapter<String> adapter;
    List<String> suggestions;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.test_add_recipe, container, false);

        ImageButton back = v.findViewById(R.id.backButton);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.container_fragment, new SavedRecipesFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        // I think this persists data throughout configuration changes like
        // screen rotations.
        setRetainInstance(true);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize adapters and attach to list views so list elements with
        // buttons are added to the ui every ime a user adds an ingredient
        // or instruction.
        AddListAdapter ingredientsAdapter = new AddListAdapter(ingredients, this.getContext());
        AddListAdapter instructionAdapter = new AddListAdapter(instructions, this.getContext());
        ListView ingredientsList = (ListView) view.findViewById(R.id.ingredients_list);
        ListView instructionsList = (ListView) view.findViewById(R.id.instructions_list);
        ingredientsList.setAdapter(ingredientsAdapter);
        instructionsList.setAdapter(instructionAdapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // If user isn't logged in or has logged out.
        if(user == null){
            getActivity().finish();
        }

        // Get places where there is user input
        recipeName = (EditText) view.findViewById(R.id.recipe_name);
        ingredient = (CustomAutoCompleteView) view.findViewById(R.id.ingredients_input);
        instruction = (EditText) view.findViewById(R.id.instruction_input);
        time = (EditText) view.findViewById(R.id.time);
        servings = (EditText) view.findViewById(R.id.servings);

        // Setup ingredient suggestions from our database
        ingredient.addTextChangedListener(new CustomAutoCompleteTextChangedListener(this, this.getContext()));
        adapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_dropdown_item_1line, suggestions);
        ingredient.setAdapter(adapter);

        // Completion istener to check if an ingredient exists in our database before
        // allowing the user to add the ingredient to their list
        OnCompleteListener<DocumentSnapshot> onExists = new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    if(doc.exists()){
                        ingredients.add(doc.getId());

                        // So that the list view gets updated properly
                        ingredientsAdapter.notifyDataSetChanged();
                    }
                    else{
                        Toast.makeText(getContext(),
                                "Sorry, we can't find this ingredient!",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    // TODO proper error logging
                    Toast.makeText(getContext(),
                            "Error ! " + task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        };

        // Click listener to begin ingredient existence check above.
        View.OnClickListener onIngredientAdd = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ingredientText = ingredient.getText().toString();

                // Don't do anything if no input
                if(ingredientText.isEmpty()) {
                    return;
                }

                Database db = new Database();
                db.getDocument("ingredients", ingredientText, onExists);
            }
        };

        // Click listener to add instructions from user input and updates
        // the list view
        View.OnClickListener onInstructionAdd = new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String instructionText = instruction.getText().toString();
                if(instructionText.isEmpty()){
                    return;
                }
                instructions.add(instructionText);
                instructionAdapter.notifyDataSetChanged();
            }
        };

        // completion listener to get user info. We do this purely to get the
        // name of the user so we can attach it to their custom recipe.
        OnCompleteListener<DocumentSnapshot> onGetUser = new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){

                    // Get all relevant data for the recipe
                    String uid = user.getUid();
                    String name = recipeName.getText().toString().trim();
                    String totalTime = time.getText().toString().trim();
                    String totalServings = servings.getText().toString().trim();
                    String username = (String) task.getResult().get("username");

                    Database db = new Database();

                    // First write the recipe to our recipes collection
                    db.write("recipes", uid + name,
                            getRecipeMap(name, totalTime, username, totalServings),
                            AddRecipeFragment.this, "", "Something went wrong!");

                    // Then write the recipe name to the user's self_made
                    // recipes list. (Note: FieldValue.arrayUnion returns an
                    // object that unions the passed in argument(s) with an
                    // existing list in the database)
                    Map<String, Object> userRecipesUpdate = new HashMap<>();
                    userRecipesUpdate.put("self_made", FieldValue.arrayUnion(uid + name));
                    userRecipesUpdate.put("saved", FieldValue.arrayUnion(uid+name));
                    db.update("user_recipes", "user_recipes_id_" + user.getUid(),
                            userRecipesUpdate, AddRecipeFragment.this, "Done!",
                            "Something went wrong!");
                }
                else{
                    // TODO proper error logging
                    Toast.makeText(getContext(),
                            "Error ! " + task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        };

        // completion listener to check if a recipe with the same name already
        // exists. The recipe name is the concatenation of the user's uid and
        // the input name. That is to say, user's can name their recipes the
        // same name as existing recipes, but they cannot create a recipe
        // with the name of a recipe they previously created.
        OnCompleteListener<DocumentSnapshot> onRecipeExists = new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    if(doc.exists()){
                        Toast.makeText(getContext(),
                                "You already made a recipe with this name!",
                                Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Database db = new Database();
                        db.getDocument("users", user.getUid(), onGetUser);
                    }
                }
                else{
                    // TODO proper error logging
                    Toast.makeText(getContext(),
                            "Error ! " + task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        };

        // click listener to begin process of writing the recipe to the database
        View.OnClickListener onSubmit = new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                // Check if all fields are filled in
                if(!isSubmittable()){
                    return;
                }

                String uid = user.getUid();
                String name = uid + recipeName.getText().toString().trim();
                Database db = new Database();
                db.getDocument("recipes", name, onRecipeExists);
            }
        };

        // click listener to reset inputs and clear lists.
        View.OnClickListener onClear = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ingredients.clear();
                instructions.clear();
                ingredientsAdapter.notifyDataSetChanged();
                instructionAdapter.notifyDataSetChanged();
                recipeName.setText("");
                time.setText("");
                servings.setText("");
                ingredient.setText("");
                instruction.setText("");
            }
        };

        // attach listeners to their buttons
        view.findViewById(R.id.add_ingredient).setOnClickListener(onIngredientAdd);
        view.findViewById(R.id.add_instruction).setOnClickListener(onInstructionAdd);
        view.findViewById(R.id.done_button).setOnClickListener(onSubmit);
        view.findViewById(R.id.clear).setOnClickListener(onClear);
    }

    /**
     * Returns a list of maps containing parsed and unparsed ingredients like
     * in the database. Although, because all of the ingredients the user input
     * will be from our database, the parsed and unparsed ingredients in the map
     * will be the same.
     * @param baseList List of ingredients where each ingredient will have their
     *                 own map.
     * @return list of maps, each containing a parsed and unparsed ingredient.
     */
    private List<Map<String, Object>> populateIngredientsList(List<String> baseList){
        List<Map<String, Object>> ingredientsList = new LinkedList<>();
        for(String ingredient : baseList){
            Map<String, Object> ingredientMapping = new HashMap<>();
            ingredientMapping.put("ingredient", ingredient);
            ingredientMapping.put("parsed_ingredient", ingredient);
            ingredientsList.add(ingredientMapping);
        }
        return ingredientsList;
    }

    /**
     * Creates and returns a map for the recipe to write to the database.
     * @param name Input name of the recipe (that is, without the user's uid).
     * @param time Time to make
     * @param user Name of the user.
     * @param servings Serving size.
     * @return A map representing the recipe to write to the database.
     */
    private Map<String, Object> getRecipeMap(String name, String time, String user, String servings){
        Map<String, Object> recipe = new HashMap<>();
        recipe.put("all_ingredients", ingredients);
        recipe.put("image", "");
        recipe.put("ingredients", populateIngredientsList(ingredients));
        recipe.put("instructions", instructions);
        recipe.put("name", name);
        recipe.put("total_time", Integer.parseInt(time));
        recipe.put("user_created", user);
        recipe.put("yield", servings + " servings(s)");
        recipe.put("likes", 0);
        recipe.put("dislikes", 0);
        recipe.put("comments", new ArrayList<Map<String, String>>());
        return recipe;
    }

    /**
     * Checks if the user inputs are sufficient for the recipe to be added to
     * the database. Outputs an error message if not.
     * @return true if the user has provided enough inputs, false otherwise.
     */
    private boolean isSubmittable(){
        String name = recipeName.getText().toString().trim();
        String totalTime = time.getText().toString().trim();
        String totalServings = servings.getText().toString().trim();
        String errorMsg = "";
        if(name.isEmpty()){
            errorMsg = "Please enter a recipe name";
        }
        else if(ingredients.isEmpty()){
            errorMsg = "Please add at least 1 ingredient";
        }
        else if(instructions.isEmpty()){
            errorMsg = "Please add at least 1 step";
        }
        else if(totalTime.isEmpty()){
            errorMsg =  "Please enter the total time to make";
        }
        else if(totalServings.isEmpty()){
            errorMsg = "Please enter the number of servings";
        }

        if(errorMsg.isEmpty()) {
            return true;
        }
        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
        return false;
    }

    /**
     * Returns the arraylist of ingredient suggestions.
     * @return the arraylist of ingredient suggestions.
     */
    public List<String> getSuggestions(){
        return suggestions;
    }

    /**
     * Replaces suggestions list with passed in list.
     * @param suggestions New suggestions to replace the current.
     */
    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    /**
     * Returns the object's adapter for ingredient suggestions.
     * @return the object's adapter
     */
    public ArrayAdapter<String> getAdapter(){
        return this.adapter;
    }

    /**
     * Replaces the object's adapter with the one passed in.
     * @param adapter New adapter to replace with the current.
     */
    public void setAdapter(ArrayAdapter<String> adapter){
        this.adapter = adapter;
    }

    /**
     * Returns the ingredient suggestion text box.
     * @return the ingredient suggestion text box.
     */
    public AutoCompleteTextView getIngredient(){
        return ingredient;
    }
}
