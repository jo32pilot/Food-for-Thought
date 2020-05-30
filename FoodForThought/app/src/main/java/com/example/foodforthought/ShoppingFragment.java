package com.example.foodforthought;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.foodforthought.Misc.Database;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

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

        return view;
    }

   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Add new item to list
        searchShopping.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(query != null) {
                    shopping_list.put(query, "1");
                    createItem(query, "1");
                    searchShopping.setQuery("", false);
                    return true;
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

       shoppingListLayout = view.findViewById(R.id.shoppingListLayout);


   }

    @Override
    public void onStop() {
        super.onStop();
        //db.update("user_ingredients", userIngredientsId, shopping_list, this, "success", "failure");
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

        //What to do if checkbox is clicked
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkBox.isChecked() ){
                    //Make black and not striked through
                    checkBox.setTextColor(Color.parseColor("#000000"));
                    checkBox.setPaintFlags(checkBox.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));

                }
                else {
                    //Strike through and make gray and move to bottom
                    checkBox.setTextColor(Color.parseColor("#D3D3D3"));
                    checkBox.setPaintFlags(checkBox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    shoppingListLayout.removeView(relativeLayout);
                    shoppingListLayout.addView(relativeLayout);
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
                db.update("user_ingredients", userIngredientsId, map, ShoppingFragment.this, "success", "failure");
            }
        });
        //What to do if minus button is clicked
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //decrease counter
                int temp = Integer.parseInt(amount.getText().toString());
                amount.setText(String.valueOf(temp - 1));
                Map<String, Object> map = new HashMap<>();
                map.put("shopping_list." + query, temp - 1);
                db.update("user_ingredients", userIngredientsId, map, ShoppingFragment.this, "success", "failure");

            }
        });

    }
}
