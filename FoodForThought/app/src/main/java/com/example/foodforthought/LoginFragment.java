package com.example.foodforthought;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

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
        return inflater.inflate(R.layout.login_fragment, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fAuth = FirebaseAuth.getInstance();
        emailId = (EditText) view.findViewById(R.id.emailLogin);
        passwordId = (EditText) view.findViewById(R.id.passwordLogin);

        //check if users already logged in
        if(fAuth.getCurrentUser() != null) {
            Intent intent = new Intent(getActivity(), HomeActivity.class);
            startActivity(intent);
        }

        view.findViewById(R.id.loginButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Clicked");
                //check if credentials in database
                if(emailId.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(),"enter email address",Toast.LENGTH_SHORT).show();
                }else {
                    if (emailId.getText().toString().trim().matches(emailPattern)) {

                        String email = emailId.getText().toString().trim();
                        String password = passwordId.getText().toString().trim();
                        //log in the user right here
                        fAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(getContext(), "Logged in Successfully", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(getActivity(), HomeActivity.class);
                                    startActivity(intent);

                                }else {
                                    Toast.makeText(getContext(), "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    } else {
                        Toast.makeText(getContext(),"Invalid email address", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        view.findViewById(R.id.signUpButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(LoginFragment.this)
                        .navigate(R.id.action_LoginFragment_to_SignUpFragment);
            }
        });
    }
}
