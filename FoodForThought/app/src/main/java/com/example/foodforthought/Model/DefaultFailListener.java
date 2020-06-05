/**
 * File that contains the default fail listener that runs if a database read/write fails.
 */
package com.example.foodforthought.Model;

import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.android.gms.tasks.OnFailureListener;

/**
 * The a read or write from the database fails, this can be used to capture error output.
 */
public class DefaultFailListener implements OnFailureListener {
    private String message;
    private Fragment frag;

    /**
     * Constructor.
     * @param message The failure message.
     * @param frag The fragment to display in.
     */
    public DefaultFailListener(String message, Fragment frag){
        this.message = message;
        this.frag = frag;
    }

    /**
     * Makes a popup on failure.
     * @param e The error.
     */
    @Override
    public void onFailure(@NonNull Exception e) {
        // error logging
        Toast.makeText(frag.getContext(), message +
                e.getMessage(), Toast.LENGTH_SHORT).show();
    }
}