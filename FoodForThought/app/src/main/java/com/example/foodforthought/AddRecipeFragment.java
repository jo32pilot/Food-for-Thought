package com.example.foodforthought;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;

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

        fAuth = FirebaseAuth.getInstance();
        recipeName = (EditText) view.findViewById(R.id.recipe_name);
        ingredient = (CustomAutoCompleteView) view.findViewById(R.id.ingredients_input);
        instruction = (EditText) view.findViewById(R.id.steps_input);

        // TODO maybe don't need this line
        suggestions = new ArrayList<>();

        ingredient.addTextChangedListener(new CustomAutoCompleteTextChangedListener(this, this.getContext()));
        adapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_dropdown_item_1line, suggestions);
        ingredient.setAdapter(adapter);


        View.OnClickListener onIngredientAdd = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ingredient.getText().toString().isEmpty()) {

                }
            }
        };

        //view.findViewById(R.id.loginButton).setOnClickListener(onIngredientAdd);
    }
}
