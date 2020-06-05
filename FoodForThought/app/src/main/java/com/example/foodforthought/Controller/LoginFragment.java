package com.example.foodforthought.Controller;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.foodforthought.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginFragment extends Fragment {
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    EditText emailId, passwordId;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth fAuth;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup firebase authentication
        fAuth = FirebaseAuth.getInstance();
        // Get views to read
        emailId = view.findViewById(R.id.emailLogin);
        passwordId = view.findViewById(R.id.passwordLogin);

        // Check if users already logged in
        if(fAuth.getCurrentUser() != null) {
            Intent intent = new Intent(getActivity(), HomeActivity.class);
            startActivity(intent);
        }

        view.findViewById(R.id.loginButton).setOnClickListener(v -> {
            System.out.println("Clicked");

            // Check if no email added
            if(emailId.getText().toString().isEmpty()) {
                Toast.makeText(getContext(),"enter email address",Toast.LENGTH_SHORT).show();
            }else {
                // Check that it is a valid email address pattern
                if (emailId.getText().toString().trim().matches(emailPattern)) {

                    // Get email and password
                    String email = emailId.getText().toString().trim();
                    String password = passwordId.getText().toString().trim();

                    // Log in the user w/ email and password
                    fAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
                        // If could log in (let user know and log them in)
                        if(task.isSuccessful()){
                            Toast.makeText(getContext(), "Logged in Successfully",
                                    Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(getActivity(), HomeActivity.class);
                            startActivity(intent);

                        }
                        // Let user know log in could not happen
                        else {
                            Toast.makeText(getContext(), "Error ! "
                                    + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
                // Let user know that an invalid email was input
                else {
                    Toast.makeText(getContext(),"Invalid email address", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Move user to sign up page
        view.findViewById(R.id.signUpButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(LoginFragment.this)
                        .navigate(R.id.action_LoginFragment_to_SingUpFragment);
            }
        });
    }
}
