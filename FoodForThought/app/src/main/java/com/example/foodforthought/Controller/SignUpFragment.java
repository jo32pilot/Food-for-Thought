package com.example.foodforthought.Controller;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.foodforthought.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SignUpFragment extends Fragment {
    static final int GOOGLE_SIGN_IN = 123;
    EditText mFirstName, mLastName, mEmail, mPassword, mConfirm;
    FirebaseAuth fAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.signup_fragment, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //signing up
        mFirstName = view.findViewById(R.id.firstNameRegister);
        mLastName = view.findViewById(R.id.lastNameRegister);
        mEmail = view.findViewById(R.id.emailRegister);
        mPassword = view.findViewById(R.id.passwordRegister);
        mConfirm = view.findViewById(R.id.passwordRegister2);

        fAuth = FirebaseAuth.getInstance();

        //check if users already logged in
        if(fAuth.getCurrentUser() != null) {
            Intent intent = new Intent(getActivity(), HomeActivity.class);
            startActivity(intent);
        }

        view.findViewById(R.id.registerButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                String confirm = mConfirm.getText().toString().trim();
                final String firstName = mFirstName.getText().toString().trim();
                final String lastName = mLastName.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    mEmail.setError("Email is Required.");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    mEmail.setError("Password is Required.");
                    return;
                }
                if(password.length() < 6) {
                    mPassword.setError("Password must be at least 6 characters long.");
                    return;
                }
                if(!password.equals(confirm)) {
                    mPassword.setError("Passwords did not match.");
                    return;
                }

                fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(getContext(), "User Created", Toast.LENGTH_SHORT).show();

                            //Store Data into database
                            FirebaseUser us = fAuth.getCurrentUser();
                            updateDB(us, firstName, lastName);

                            Intent intent = new Intent(getActivity(), HomeActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getContext(), " Error! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        view.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SignUpFragment.this)
                        .navigate(R.id.action_SignUpFragment_to_LoginFragment);
            }
        });
    }

    private void updateDB (FirebaseUser us, String first, String last) {
        Map<String, Integer> inventory_m = new HashMap<>();
        Map<String, Integer> shopping_m = new HashMap<>();
        List<String> favourite_l = new ArrayList<String>();
        List<String> liked_l = new ArrayList<String>();
        List<String> disliked_l = new ArrayList<String>();
        List<String> saved_l = new ArrayList<String>();
        List<String> self_made_l = new ArrayList<String>();

        Map<String, Object> user = new HashMap<>();
        user.put("username", first + " " + last);
        user.put("email", us.getEmail());
        user.put("user_ingredients_id", "user_ingredients_id_" + us.getUid());
        user.put("user_recipes_id", "user_recipes_id_" + us.getUid());

        //Add a new document with a UID, users collection
        db.collection("users")
                .document(us.getUid())
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        System.out.println("Successfully added to db.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("Failed");
                    }
                });

        Map<String, Object> user_ingredients = new HashMap<>();
        user_ingredients.put("inventory", inventory_m);
        user_ingredients.put("shopping_list", shopping_m);

        //user_ingredients collection
        db.collection("user_ingredients")
                .document("user_ingredients_id_" + us.getUid())
                .set(user_ingredients)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        System.out.println("Successfully added to db.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("Failed");
                    }
                });

        Map<String, Object> user_recipes = new HashMap<>();
        user_recipes.put("favourite", favourite_l);
        user_recipes.put("liked", liked_l);
        user_recipes.put("disliked", disliked_l);
        user_recipes.put("saved", saved_l);
        user_recipes.put("self_made", self_made_l);


        //user_ingredients collection
        db.collection("user_recipes")
                .document("user_recipes_id_" + us.getUid())
                .set(user_recipes)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        System.out.println("Successfully added to db.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("Failed");
                    }
                });
    }
}
