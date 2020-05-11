package com.example.foodforthought;

import android.os.Bundle;
import android.view.MenuItem;
import com.example.foodforthought.MainFragment;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener;

// Activity that hosts fragments for recipe, inventory, and shopping pages
public class HomeActivity  extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    // Object declarations for activity class
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    NavigationView navigationView;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

     // onCreate instantiates objects with objects from UI and assigns them to the above declarations
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Instantiate objects from the UI with objects declared in this class
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer);
        // Initializes navbar
        navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);

        // Adds a listener to check if a user clicks on the drawer layout a.k.a the navbar
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();


        // Load default fragment a.k.a. the first fragment after the login page is the main feed fragment
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.container_fragment, new MainFragment());
        fragmentTransaction.commit();
    }
    // Switch between items on the navbar
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Allows users to navigate between items on the navbar by clicking Home, Recipe, Inventory, Shopping

        // Switches to main feed page when home button clicked in navbar
        if (item.getItemId() == R.id.HomeItem){
            fragmentManager = getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_fragment, new MainFragment());
            fragmentTransaction.commit();
        }

        // Switches to main feed page when recipe button clicked in navbar
        if (item.getItemId() == R.id.RecipeItem){
            fragmentManager = getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_fragment, new AddRecipeFragment());
            fragmentTransaction.commit();

        }

        // Switches to main feed page when inventory button clicked in navbar
        if (item.getItemId() == R.id.InventoryItem){
            fragmentManager = getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_fragment, new InventoryFragment());
            fragmentTransaction.commit();

        }

        // Switches to main feed page when shopping button clicked in navbar
        if (item.getItemId() == R.id.ShoppingItem){
            fragmentManager = getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_fragment, new ShoppingFragment());
            fragmentTransaction.commit();

        }

        return true;
    }
}
