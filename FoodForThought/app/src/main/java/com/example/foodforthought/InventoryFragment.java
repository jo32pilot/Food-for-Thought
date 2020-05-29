package com.example.foodforthought;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
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

import java.util.List;

public class InventoryFragment extends Fragment {

    private SearchView searchIngredients;
    private LinearLayout pantryListLayout;
    private Database db = new Database();
    private List<String> userInventory;
    private List<String> userInventoryAmts;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inventory, container, false);
        searchIngredients = view.findViewById(R.id.searchInv);
        pantryListLayout = view.findViewById(R.id.pantryListLayout);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        TextView recipeFeed = view.findViewById(R.id.testText);

        // If user isn't logged in or has logged out.
        if (user == null) {
            NavHostFragment.findNavController(InventoryFragment.this)
                    .navigate(R.id.action_InventoryFragment_to_LoginFragment);
        }

        String uid = user.getUid();
        String userIngredientsId = "user_ingredients_id_" + uid;

        // Where it all starts
        db.getDocument("user_ingredients", userIngredientsId, onGetUserIngredients);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        searchIngredients.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // db.getDocument("ingredients", onGetAllIngredients); (Look at John's code to double check)
                // searchIngredients.getSuggestionsAdapter();
                return true;
            }
        });

    }

    OnCompleteListener<DocumentSnapshot> onGetAllIngredients = new OnCompleteListener<DocumentSnapshot>() {
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            if(task.isSuccessful()){
                //Get all ingredients that start with newText
                //Limit to 10
                //Put into suggestionsAdapter
                //Only allow clicking on suggestions?
            }
        }
    };

    // Listener for when we've received the user's ingredients.
    OnCompleteListener<DocumentSnapshot> onGetUserIngredients = new OnCompleteListener<DocumentSnapshot>() {

        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            if(task.isSuccessful()){
                DocumentSnapshot userIngredients = task.getResult();
                if(userIngredients != null){
                    userInventory = (List<String>) userIngredients.get("inventory");
                    userInventoryAmts = (List<String>) userIngredients.get("inventory_amount");
                    if(userInventory != null && userInventoryAmts != null){
                        setUpScreen();
                    }
                }
            }
        }
    };

    protected void setUpScreen(){
        for(int i = 0; i < userInventory.size(); i++){
            createItem(userInventory.get(i), userInventoryAmts.get(i));
        }
    }

    private void createItem(String name, String number){
        RelativeLayout relativeLayout = new RelativeLayout(this.getContext());
        relativeLayout.setId(View.generateViewId());
        relativeLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        //Create all items of new row
        EditText item = new EditText(this.getContext());
        ImageButton minusButton = new ImageButton(this.getContext());
        ImageButton plusButton = new ImageButton(this.getContext());
        EditText amount = new EditText(this.getContext());

        //Set valid IDs
        item.setId(View.generateViewId());
        minusButton.setId(View.generateViewId());
        plusButton.setId(View.generateViewId());
        amount.setId(View.generateViewId());

        //add items of new row into new row
        relativeLayout.addView(item);
        relativeLayout.addView(amount);
        relativeLayout.addView(plusButton);
        relativeLayout.addView(minusButton);

        //add row to list
        pantryListLayout.addView(relativeLayout);

        //Set parameters for checkbox (left side)
        RelativeLayout.LayoutParams itemParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        itemParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        item.setLayoutParams(itemParams);
        //Set text to ingredient
        item.setText(name);
        //Set size of text
        item.setTextSize(TypedValue.COMPLEX_UNIT_SP,30);

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
        amount.setText(number);


        //What to do if plus buton is clicked
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //increment counter
                int temp = Integer.parseInt(amount.getText().toString());
                amount.setText(String.valueOf(temp + 1));
            }
        });

        //What to do if minus button is clicked
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //decrease counter
                int temp = Integer.parseInt(amount.getText().toString());
                amount.setText(String.valueOf(temp - 1));
            }
        });

    }



       /*// Listener for when we've received ingredients from the database.
       OnCompleteListener<QuerySnapshot> onGetIngredients = new OnCompleteListener<QuerySnapshot>() {
           @Override
           public void onComplete(@NonNull Task<QuerySnapshot> task) {
               if (task.isSuccessful()) {
                   for(QueryDocumentSnapshot doc : task.getResult()){
                       Log.d("Ingredients List: ", (String) doc.get("name"));
                   }

               }
           }
       };

       CollectionReference ingredientsRef = db.getDB().collection("ingredients");
       ingredientsRef.orderBy("name")
               .limit(10)
               .get()
               .addOnCompleteListener(onGetIngredients);

       searchIngredients.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_GO){
                    return true;
                }
                return false;
            }
        });*/
}
