package com.example.foodforthought;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;

public class DefaultFailListener implements OnFailureListener {

    private String message;
    private Fragment frag;

    public DefaultFailListener(String message, Fragment frag){
        this.message = message;
        this.frag = frag;
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        Toast.makeText(frag.getContext(), message +
                e.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
