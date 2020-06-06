/**
 * The file containing the functionality of the signup page.
 *
 * @author Ashley Eckbert
 */
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

/**
 * The signup fragment allows the user to create a new account with a new
 * username and password.
 */
public class SignUpFragment extends Fragment {
    // constants
    private static final int MIN_PASSWORD_LENGTH = 6;

    // views and variables
    EditText mFirstName, mLastName, mEmail, mPassword, mConfirm;
    FirebaseAuth fAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Builds the view when the fragment is opened.
     * @param inflater Inflated view to fit the screen.
     * @param container What the screen is contained in.
     * @param savedInstanceState Persists data throughout configuration changes.
     * @return The fully built view.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }

    /**
     * After the view is created, add all the functionality. That is, the signup feature that
     * get the username and password from the text boxes and makes a new account in the database.
     * @param view
     * @param savedInstanceState
     */
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

        // when the register button is clicked
        view.findViewById(R.id.registerButton).setOnClickListener(new View.OnClickListener() {
            /**
             * When thre register button is clicked, create a new account in the database.
             * @param v The view of the register button.
             */
            @Override
            public void onClick(View v) {
                // user input
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                String confirm = mConfirm.getText().toString().trim();
                final String firstName = mFirstName.getText().toString().trim();
                final String lastName = mLastName.getText().toString().trim();

                // invalid input handling
                if(TextUtils.isEmpty(email)){
                    mEmail.setError("Email is Required.");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    mEmail.setError("Password is Required.");
                    return;
                }
                if(password.length() < MIN_PASSWORD_LENGTH) {
                    mPassword.setError("Password must be at least 6 characters long.");
                    return;
                }
                if(!password.equals(confirm)) {
                    mPassword.setError("Passwords did not match.");
                    return;
                }

                // function which create new user
                fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(
                        new OnCompleteListener<AuthResult>() {
                    /**
                     * Once the new account has been made, let the current user now.
                     * Then log them into the app.
                     * @param task
                     */
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(getContext(), "User Created",
                                    Toast.LENGTH_SHORT).show();

                            // Store Data into database
                            FirebaseUser us = fAuth.getCurrentUser();
                            updateDB(us, firstName, lastName);

                            // start main functionality of application
                            Intent intent = new Intent(getActivity(), HomeActivity.class);
                            startActivity(intent);
                        }
                        else {
                            Toast.makeText(getContext(), " Error! " +
                                            task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        // when the cancel button is clicked
        view.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            /**
             * After the cancel button is clicked, go back to the login page.
             * @param view The view of the cancel button.
             */
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SignUpFragment.this)
                        .navigate(R.id.action_SignUpFragment_to_LoginFragment);
            }
        });
    }

    /**
     * Update the database with all the fields necessary for a new user, that is:
     * inventory list, shopping list, like, dislike, favorite lists, saved list,
     * selfmade list... etc
     * @param us Firebase user info
     * @param first User's first name
     * @param last User's last name
     */
    private void updateDB (FirebaseUser us, String first, String last) {
        // empty lists in the user_ingredients and user_recipes fields
        Map<String, Integer> inventory_m = new HashMap<>();
        Map<String, Integer> shopping_m = new HashMap<>();
        List<String> favourite_l = new ArrayList<String>();
        List<String> liked_l = new ArrayList<String>();
        List<String> disliked_l = new ArrayList<String>();
        List<String> saved_l = new ArrayList<String>();
        List<String> self_made_l = new ArrayList<String>();

        // for "user" collection in the database
        Map<String, Object> user = new HashMap<>();
        user.put("username", first + " " + last);
        user.put("email", us.getEmail());
        user.put("user_ingredients_id", "user_ingredients_id_" + us.getUid());
        user.put("user_recipes_id", "user_recipes_id_" + us.getUid());

        // Add a new document with a UID, users collection
        db.collection("users")
                .document(us.getUid())
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    /**
                     * If adding the user succeeds, let them know.
                     * @param aVoid unused
                     */
                    @Override
                    public void onSuccess(Void aVoid) {
                        System.out.println("Successfully added to db.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    /**
                     * If adding the user fails, let them know.
                     * @param e unused
                     */
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("Failed");
                    }
                });

        // put blank fields in
        Map<String, Object> user_ingredients = new HashMap<>();
        user_ingredients.put("inventory", inventory_m);
        user_ingredients.put("shopping_list", shopping_m);

        // user_ingredients collection
        db.collection("user_ingredients")
                .document("user_ingredients_id_" + us.getUid())
                .set(user_ingredients)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    /**
                     * If adding the user ingredients succeeds, let them know.
                     * @param aVoid unused
                     */
                    @Override
                    public void onSuccess(Void aVoid) {
                        System.out.println("Successfully added to db.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    /**
                     * If adding the user ingredients fails, let them know.
                     * @param e unused
                     */
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("Failed");
                    }
                });

        // put blank fields in
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
                    /**
                     * If adding the user recipes succeeds, let them know.
                     * @param aVoid unused
                     */
                    @Override
                    public void onSuccess(Void aVoid) {
                        System.out.println("Successfully added to db.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    /**
                     * If adding the user recipes fails, let them know.
                     * @param e unused
                     */
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("Failed");
                    }
                });
    }
}