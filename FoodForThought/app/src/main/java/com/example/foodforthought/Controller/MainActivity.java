/**
 * The file containing the main method of the app. This controller is the first thing ran
 * once the user opens the application.
 */
package com.example.foodforthought.Controller;

import android.os.Bundle;
import com.example.foodforthought.R;
import com.google.firebase.FirebaseApp;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

/**
 * The first thing ran by the app.
 * Starts everything up.
 */
public class MainActivity extends AppCompatActivity {
    /**
     * When the app is started.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // initializes the application with the database
        FirebaseApp.initializeApp(this);

        // set the first screen the be the activity_main layout, ie the login screen
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Adds items to the navigation bar.
     * @param menu The menu with the navbar items.
     * @return Success or failure.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Handle the action bar item clicks.
     * @param item The selected item.
     * @return Success or failure.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}