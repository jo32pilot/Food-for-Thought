/**
 * File that contains the default success listener that runs if a database read/write succeeds.
 */
package com.example.foodforthought.Model;

import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;

/**
 * The a read or write from the database succeeds, this can be used to display success
 * to the user.
 */
public class DefaultSuccessListener<T> implements OnSuccessListener<T> {
    private String message;
    private Fragment frag;

    /**
     * Constructor
     * @param message The success message
     * @param frag The fragment we are displaying in.
     */
    public DefaultSuccessListener(String message, Fragment frag){
        this.message = message;
        this.frag = frag;
    }

    /**
     * Makes a popup on succes.
     * @param t Unused
     */
    @Override
    public void onSuccess(T t) {
        // success logging
        Toast.makeText(frag.getContext(),
                message, Toast.LENGTH_SHORT).show();
    }
}