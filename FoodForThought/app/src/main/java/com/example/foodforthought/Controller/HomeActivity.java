/**
 * File holds functionality for allowing the user to control the navbar at the bottom of
 * the screen.
 */
package com.example.foodforthought.Controller;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.foodforthought.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Controls the navbar that switches between the main fragments in the app
 */
public class HomeActivity extends AppCompatActivity {
    // Object declarations for activity class
    BottomNavigationView bottomNavigationView;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    /**
     * onCreate instantiates objects with objects from UI and assigns them to the above declarations
     * @param savedInstanceState Persists data throughout configuration changes.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Instantiate objects from the UI with objects declared in this class
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Initializes navbar
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        // Load default fragment a.k.a. the first fragment after the login page is the main feed fragment
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.container_fragment, new MainFragment());
        fragmentTransaction.commit();
    }

    // Switch between items on the bottom navbar
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                /**
                 * When the user clicks on one of the navbar items.
                 * @param item Either the profile, shopping screen, pantry, saved recipe list, or
                 *             home feed.
                 * @return True if successful.
                 */
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    // Switches to main feed page when home button clicked in navbar
                    if (item.getItemId() == R.id.HomeItem){
                        fragmentManager = getSupportFragmentManager();
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.container_fragment, new MainFragment());
                        fragmentTransaction.commit();
                    }

                    // Switches to saved recipe page when recipe button clicked in navbar
                    if (item.getItemId() == R.id.SavedRecipesItem){
                        fragmentManager = getSupportFragmentManager();
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.container_fragment, new SavedRecipesFragment());
                        fragmentTransaction.commit();
                    }

                    // Switches to inventory page page when inventory button clicked in navbar
                    if (item.getItemId() == R.id.InventoryItem){
                        fragmentManager = getSupportFragmentManager();
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.container_fragment, new InventoryFragment());
                        fragmentTransaction.commit();
                    }

                    // Switches to shopping page when shopping button clicked in navbar
                    if (item.getItemId() == R.id.ShoppingItem){
                        fragmentManager = getSupportFragmentManager();
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.container_fragment, new ShoppingFragment());
                        fragmentTransaction.commit();
                    }

                    // Switches to profile page when shopping button clicked in navbar
                    if (item.getItemId() == R.id.UserProfileItem){
                        fragmentManager = getSupportFragmentManager();
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.container_fragment, new UserProfileFragment());
                        fragmentTransaction.commit();
                    }
                    return true;
                }
            };
}