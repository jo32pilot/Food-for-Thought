package com.example.foodforthought;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class CustomAutoCompleteTextChangedListener implements TextWatcher {

    private Fragment frag;
    private Context context;
    private FirebaseFirestore db;


    private static final int MIN_CHAR = 3;

    public CustomAutoCompleteTextChangedListener(Fragment frag, Context context){
        this.frag = frag;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();

    }

    @Override
    public void afterTextChanged(Editable s) { }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) { }

    @Override
    public void onTextChanged(CharSequence userInput, int start, int before, int count) {

        AddRecipeFragment addRecipeFragment = (AddRecipeFragment) frag;

        System.out.println("TEXT CHANGED");
        OnCompleteListener<QuerySnapshot> onGetIngredients = new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    ArrayList<String> suggestions = new ArrayList<>();
                    for(QueryDocumentSnapshot ingredient : task.getResult()){
                        suggestions.add((String) ingredient.get("name"));
                    }

                    addRecipeFragment.suggestions = suggestions;
                    addRecipeFragment.adapter.notifyDataSetChanged();
                    addRecipeFragment.adapter = new ArrayAdapter<>(context,
                            android.R.layout.simple_dropdown_item_1line, addRecipeFragment.suggestions);
                    addRecipeFragment.ingredient.setAdapter(addRecipeFragment.adapter);
                }
                else{
                    System.out.println("OOP");
                    // TODO have toast send some text (something like couldn't find ingredients)
                }
            }
        };

        db.collection("ingredients")
                .whereGreaterThanOrEqualTo("name",
                        userInput.toString()).get().addOnCompleteListener(onGetIngredients);

        /*
        MainActivity mainActivity = ((MainActivity) context);

        // query the database based on the user input
        mainActivity.item = mainActivity.getItemsFromDb(userInput.toString());

        // update the adapater
        mainActivity.myAdapter.notifyDataSetChanged();
        mainActivity.myAdapter = new ArrayAdapter<String>(mainActivity, android.R.layout.simple_dropdown_item_1line, mainActivity.item);
        mainActivity.myAutoComplete.setAdapter(mainActivity.myAdapter);
        */
    }

}