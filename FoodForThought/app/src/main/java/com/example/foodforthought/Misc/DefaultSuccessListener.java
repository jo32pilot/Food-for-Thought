package com.example.foodforthought.Misc;

import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;

public class DefaultSuccessListener<T> implements OnSuccessListener<T> {

    private String message;
    private Fragment frag;

    public DefaultSuccessListener(String message, Fragment frag){
        this.message = message;
        this.frag = frag;
    }

    @Override
    public void onSuccess(T t) {
        Toast.makeText(frag.getContext(),
                message, Toast.LENGTH_SHORT).show();
    }
}
