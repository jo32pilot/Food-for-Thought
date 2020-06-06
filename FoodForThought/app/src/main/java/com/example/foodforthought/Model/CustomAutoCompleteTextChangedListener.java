/**
 * File that contains the functionality for the autocomplete feature in the AddRecipeFragment.
 *
 * @author John Li
 */
package com.example.foodforthought.Model;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.foodforthought.Controller.AddRecipeFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;

/**
 * Auto-completes suggested ingredients in the add recipe page.
 */
public class CustomAutoCompleteTextChangedListener implements TextWatcher {
    private Fragment frag;
    private Context context;
    private FirebaseFirestore db;

    // maximum number of documents in the autocomplete list
    private static final int MAX_DOCS = 10;

    /**
     * Constructor.
     * @param frag The fragment we are listening in.
     * @param context The context we are listening in.
     */
    public CustomAutoCompleteTextChangedListener(Fragment frag, Context context) {
        this.frag = frag;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();

    }

    /**
     * Defaults out.
     * @param s Unused
     */
    @Override
    public void afterTextChanged(Editable s) {}

    /**
     * Defaults out.
     * @param s Unused
     * @param start Unused
     * @param count Unused
     * @param after Unused
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    /**
     * When the text changes, suggest to the user possible ingredients matching their
     * initial input.
     * @param userInput What has been typed so far.
     * @param start Unused
     * @param before Unused
     * @param count Unused
     */
    @Override
    public void onTextChanged(CharSequence userInput, int start, int before, int count) {
        AddRecipeFragment addRecipeFragment = (AddRecipeFragment) frag;

        // when we get the ingredients from the database
        OnCompleteListener<QuerySnapshot> onGetIngredients =
                new OnCompleteListener<QuerySnapshot>() {
            /**
             * After getting the ingredients from the database, get the ones that fit
             * the userInput. Uses these as suggestions.
             * @param task Ingredient data.
             */
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<String> suggestions = new ArrayList<>();

                    // add viable ingredients to the list
                    for (QueryDocumentSnapshot ingredient : task.getResult()) {
                        suggestions.add((String) ingredient.get("name"));
                    }

                    // display these suggestions
                    addRecipeFragment.setSuggestions(suggestions);
                    addRecipeFragment.getAdapter().notifyDataSetChanged();
                    addRecipeFragment.setAdapter(new ArrayAdapter<>(context,
                            android.R.layout.simple_dropdown_item_1line,
                            addRecipeFragment.getSuggestions()));
                    addRecipeFragment.getIngredient().setAdapter(addRecipeFragment.getAdapter());
                }
                else {
                    // error logging
                    System.out.println("Something went wrong!");
                }
            }
        };
        db.collection("ingredients")
                .whereGreaterThanOrEqualTo("name", userInput.toString())
                .limit(MAX_DOCS)
                .get()
                .addOnCompleteListener(onGetIngredients);
    }
}