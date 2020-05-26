package com.example.foodforthought;

import android.os.Bundle;
import android.text.InputType;
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
        LinearLayout shoppingListLayout = view.findViewById(R.id.shoppingListLayout);

        RelativeLayout linearLayout = new RelativeLayout(this.getContext());
        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        CheckBox checkBox = new CheckBox(this.getContext());
        ImageButton minusButton = new ImageButton(this.getContext());
        ImageButton plusButton = new ImageButton(this.getContext());
        EditText amount = new EditText(this.getContext());

        linearLayout.addView(checkBox);
        linearLayout.addView(amount);
        linearLayout.addView(plusButton);
        linearLayout.addView(minusButton);
        shoppingListLayout.addView(linearLayout);

        RelativeLayout.LayoutParams checkParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        checkParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        checkBox.setLayoutParams(checkParams);
        checkBox.setText(searchIngredients.getText());
        checkBox.setTextSize(TypedValue.COMPLEX_UNIT_SP,30);

        RelativeLayout.LayoutParams minusParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        minusParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        minusParams.setMargins(0,0,20,0);
        minusButton.setLayoutParams(minusParams);
        minusButton.setImageResource(R.drawable.ic_action_min);

        RelativeLayout.LayoutParams plusParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        plusParams.addRule(RelativeLayout.RIGHT_OF, minusButton.getId());
        minusParams.setMargins(0,0,20,0);
        plusButton.setLayoutParams(plusParams);
        plusButton.setImageResource(R.drawable.ic_action_add);

        RelativeLayout.LayoutParams amountParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        minusParams.setMargins(0,0,10,0);
        amountParams.addRule(RelativeLayout.RIGHT_OF, plusButton.getId());
        amount.setLayoutParams(amountParams);
        amount.setGravity(Gravity.CENTER);
        amount.setInputType(InputType.TYPE_CLASS_TEXT);
        amount.setText("1");
    }

}
