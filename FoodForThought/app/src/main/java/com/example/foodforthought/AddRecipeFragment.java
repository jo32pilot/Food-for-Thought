package com.example.foodforthought;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AddRecipeFragment extends Fragment {

    LinkedList<String> ingredients;
    LinkedList<String> instructions;
    EditText recipeName, instruction;
    AutoCompleteTextView ingredient;
    ArrayAdapter<String> adapter;
    List<String> suggestions;
    FirebaseAuth fAuth;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.test_add_recipe, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ingredients = new LinkedList<>();
        instructions = new LinkedList<>();
        AddListAdapter ingredientsAdapter = new AddListAdapter(ingredients, this.getContext());
        ListView ingredientsList = (ListView) view.findViewById(R.id.ingredients_list);
        ingredientsList.setAdapter(ingredientsAdapter);

        fAuth = FirebaseAuth.getInstance();
        recipeName = (EditText) view.findViewById(R.id.recipe_name);
        ingredient = (CustomAutoCompleteView) view.findViewById(R.id.ingredients_input);
        instruction = (EditText) view.findViewById(R.id.steps_input);

        ingredient.addTextChangedListener(new CustomAutoCompleteTextChangedListener(this, this.getContext()));
        adapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_dropdown_item_1line, suggestions);
        ingredient.setAdapter(adapter);

        OnCompleteListener<DocumentSnapshot> onExists = new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    if(doc.exists()){
                        ingredients.add(doc.getId());
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

        View.OnClickListener onIngredientAdd = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ingredientText = ingredient.getText().toString();
                if(ingredientText.isEmpty()) {
                    return;
                }
                Database db = new Database();
                db.getDocument("ingredients", ingredientText, onExists);
            }
        };

        view.findViewById(R.id.add_ingredient).setOnClickListener(onIngredientAdd);
    }
}
