package com.example.foodforthought;

import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.method.TransformationMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class ShoppingFragment extends Fragment {
    EditText searchIngredients;
    ImageButton searchButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shopping, container, false);
        return view;
    }

   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        searchIngredients = view.findViewById(R.id.searchIngredients);
        searchButton = view.findViewById(R.id.searchButton);

        // If user isn't logged in or has logged out.
        if(user == null){
            // TODO redirect to login page
        }

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createItem(view);
                searchIngredients.setText("");
            }
        });

       //String uid = user.getUid();
        //String userIngredientsId = "user_ingredients_id_" + uid;
        //Database db = new Database();



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

    protected void createItem(@NonNull View view) {
        // Get layout to put the new row into
        LinearLayout shoppingListLayout = view.findViewById(R.id.shoppingListLayout);

        //create new row
        RelativeLayout relativeLayout = new RelativeLayout(this.getContext());
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
        checkBox.setText(searchIngredients.getText());
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
        amount.setText("1");

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkBox.isChecked() ){
                    checkBox.setTextColor(Color.parseColor("#000000"));
                    checkBox.setPaintFlags(checkBox.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
                }
                else {
                    checkBox.setTextColor(Color.parseColor("#D3D3D3"));
                    checkBox.setPaintFlags(checkBox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }
            }
        });
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = Integer.parseInt(amount.getText().toString());
                amount.setText(String.valueOf(temp + 1));
            }
        });
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = Integer.parseInt(amount.getText().toString());
                amount.setText(String.valueOf(temp - 1));
            }
        });
    }

}
