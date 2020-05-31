package com.example.foodforthought;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.foodforthought.Misc.Database;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ShoppingFragment extends Fragment {
    private SearchView searchShopping;
    private ArrayList<String> items = new ArrayList<>();
    private ArrayList<String> amounts = new ArrayList<>();
    private LinearLayout shoppingListLayout;
    private Database db = new Database();
    private String userIngredientsId;
    private Map<String, Object> shopping_list = new HashMap<>();

    private CollectionReference inventoryRef;
    private SimpleCursorAdapter mAdapter;
    private String[] SUGGESTIONS = new String[10];

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shopping, container, false);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        searchShopping = view.findViewById(R.id.searchShopping);

        // If user isn't logged in or has logged out.
        if(user == null){
            NavHostFragment.findNavController(ShoppingFragment.this)
                    .navigate(R.id.action_ShoppingFragment_to_LoginFragment);        }

        String uid = user.getUid();
        userIngredientsId = "user_ingredients_id_" + uid;

        db.getDocument("user_ingredients", userIngredientsId, onGetShoppingList);

        inventoryRef = db.getDB().collection("ingredients");

        final String[] from = new String[] {"ingredient"};
        final int[] to = new int[] {android.R.id.text1};
        mAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_1,
                null,
                from,
                to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        return view;
    }

   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchShopping.setSuggestionsAdapter(mAdapter);
        searchShopping.setIconifiedByDefault(false);

       shoppingListLayout = view.findViewById(R.id.shoppingListLayout);

       searchShopping.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
           @Override
           public boolean onSuggestionSelect(int position) {
               return false;
           }

           @Override
           public boolean onSuggestionClick(int position) {
               Cursor cursor = (Cursor) mAdapter.getItem(position);
               String txt = cursor.getString(cursor.getColumnIndex("ingredient"));
               if(txt != null) {
                   shopping_list.put(txt, "1");
                   createItem(txt, "1");
                   searchShopping.setQuery("", false);
                   return true;
               }
               return false;
           }
       });

        //Add new item to list
        searchShopping.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                inventoryRef.whereGreaterThanOrEqualTo("name", newText)
                        .limit(10)
                        .get()
                        .addOnCompleteListener(onGetAllIngredients);
                return true;
            }
        });
   }

    OnCompleteListener<QuerySnapshot> onGetAllIngredients = new OnCompleteListener<QuerySnapshot>() {
        @Override
        public void onComplete(@NonNull Task<QuerySnapshot> task) {

            int i = 0;
            for(QueryDocumentSnapshot doc : task.getResult()){
                SUGGESTIONS[i] = doc.getId().toString();
                i++;
            }
            populateAdapter();
        }
    };

    private void populateAdapter() {
        final MatrixCursor c = new MatrixCursor(new String[]{ BaseColumns._ID, "ingredient" });
        for (int i=0; i<SUGGESTIONS.length; i++) {
            c.addRow(new Object[] {i, SUGGESTIONS[i]});
        }
        mAdapter.changeCursor(c);
    }

    OnCompleteListener<DocumentSnapshot> onGetShoppingList = new OnCompleteListener<DocumentSnapshot>() {

        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            if(task.isSuccessful()){
                DocumentSnapshot userIngredients = task.getResult();
                if(userIngredients != null){
                    shopping_list = (Map<String, Object>) userIngredients.get("shopping_list");
                    if(shopping_list != null) {
                        for (String key : shopping_list.keySet()) {
                            createItem(key, shopping_list.get(key).toString());
                        }
                    }
                    else{
                        shopping_list = new HashMap<>();
                    }
                }
            }
        }
    };


    protected void createItem(String query, String amount1) {
        //create new row
        Map<String, Object> updatedMap = new HashMap<>();
        updatedMap.put("shopping_list", shopping_list);
        db.update("user_ingredients", userIngredientsId, updatedMap, this, "success", "failure");
        RelativeLayout relativeLayout = new RelativeLayout(this.getContext());
        relativeLayout.setId(View.generateViewId());
        relativeLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        //Create all items of new row
        CheckBox checkBox = new CheckBox(this.getContext());
        ImageButton minusButton = new ImageButton(this.getContext());
        ImageButton plusButton = new ImageButton(this.getContext());
        EditText amount = new EditText(this.getContext());

        //Set valid IDs
        checkBox.setId(View.generateViewId());
        minusButton.setId(View.generateViewId());
        plusButton.setId(View.generateViewId());
        amount.setId(View.generateViewId());

        //add items of new row into new row
        relativeLayout.addView(checkBox);
        relativeLayout.addView(amount);
        relativeLayout.addView(plusButton);
        relativeLayout.addView(minusButton);
        //add row to list
        shoppingListLayout.addView(relativeLayout);

        //Set parameters for checkbox (left side)
        RelativeLayout.LayoutParams checkParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        checkParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        checkBox.setLayoutParams(checkParams);
        //Set text to ingredient
        checkBox.setText(query);
        //Set size of text
        checkBox.setTextSize(TypedValue.COMPLEX_UNIT_SP,30);

        //Set parameters for minus button (right side)
        RelativeLayout.LayoutParams minusParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        minusParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        minusParams.setMarginEnd(20);
        minusButton.setLayoutParams(minusParams);
        //Set image
        minusButton.setImageResource(R.drawable.ic_action_min);

        //Set parameters for plus button (left of minus button)
        RelativeLayout.LayoutParams plusParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        Log.d("MinusButton ID: ", String.valueOf(minusButton.getId()));
        plusParams.addRule(RelativeLayout.START_OF, minusButton.getId());
        plusParams.setMarginEnd(20);
        plusButton.setLayoutParams(plusParams);
        //Set image
        plusButton.setImageResource(R.drawable.ic_action_add);

        //Set parameters for amount of (left of plus button)
        RelativeLayout.LayoutParams amountParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        amountParams.addRule(RelativeLayout.START_OF, plusButton.getId());
        amountParams.setMarginEnd(20);
        amount.setLayoutParams(amountParams);
        //Move Text of number to center (hopefully)
        amount.setGravity(Gravity.CENTER);
        //Set input type to numbers
        amount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
        //Set base amount to 1
        amount.setText(amount1);
        amount.setTextSize(TypedValue.COMPLEX_UNIT_SP,25);
        amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s == null){

                }
                else if(s.toString().isEmpty()){

                }
                else if(Integer.valueOf(s.toString()) <= 0){
                    shopping_list.remove(query);
                    Map<String, Object> updatedMap = new HashMap<>();
                    updatedMap.put("shopping_list", shopping_list);
                    db.update("user_ingredients", userIngredientsId, updatedMap,
                            ShoppingFragment.this, "",
                            "Could not remove");
                    shoppingListLayout.removeView(relativeLayout);
                }
            }
        });

        //What to do if checkbox is clicked
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkBox.isChecked() ){
                    /*//Make black and not striked through
                    checkBox.setTextColor(Color.parseColor("#000000"));
                    checkBox.setPaintFlags(checkBox.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
                    shopping_list.put(query, Integer.valueOf(amount.getText().toString()));
                    Map<String, Object> updatedMap = new HashMap<>();
                    updatedMap.put("shopping_list", shopping_list);
                    db.update("user_ingredients", userIngredientsId,
                            updatedMap, ShoppingFragment.this,
                            "", "Could not add");*/
                }
                else {
                    /*//Strike through and make gray and move to bottom
                    checkBox.setTextColor(Color.parseColor("#D3D3D3"));
                    checkBox.setPaintFlags(checkBox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);*/
                    shoppingListLayout.removeView(relativeLayout);
                    /*shoppingListLayout.addView(relativeLayout);*/
                    shopping_list.remove(query);
                    Map<String, Object> updatedMap = new HashMap<>();
                    Map<String, Object> updateInventory = new HashMap<>();
                    updateInventory.put("inventory." + query, Integer.valueOf(amount.getText().toString()));
                    updatedMap.put("shopping_list", shopping_list);
                    db.update("user_ingredients", userIngredientsId,
                            updatedMap, ShoppingFragment.this, "",
                            "Could not remove");
                    db.update("user_ingredients", userIngredientsId,
                            updateInventory, ShoppingFragment.this, "",
                            "Could not add to inventory");
                }
            }
        });
        //What to do if plus buton is clicked
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //increment counter
                int temp = Integer.parseInt(amount.getText().toString());
                amount.setText(String.valueOf(temp + 1));
                Map<String, Object> map = new HashMap<>();
                map.put("shopping_list." + query, temp + 1);
                db.update("user_ingredients", userIngredientsId,
                        map, ShoppingFragment.this, "success",
                        "failure");
            }
        });
        //What to do if minus button is clicked
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //decrease counter
                int temp = Integer.parseInt(amount.getText().toString());
                Map<String, Object> map = new HashMap<>();
                map.put("shopping_list." + query, temp - 1);
                db.update("user_ingredients", userIngredientsId,
                        map, ShoppingFragment.this, "success",
                        "failure");
                amount.setText(String.valueOf(temp - 1));

            }
        });

    }
}
