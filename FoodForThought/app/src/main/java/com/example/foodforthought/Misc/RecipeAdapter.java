package com.example.foodforthought.Misc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.foodforthought.MainFragment;
import com.example.foodforthought.R;
import com.example.foodforthought.RecipeFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import androidx.fragment.app.FragmentTransaction;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static java.security.AccessController.getContext;

/**
 * Adapter class for the Recipe class
 * Objective: Convert a recipe object at a certain position into an item to be inserted into the RecyclerView
 */

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.viewHolder> {
    // Objects of recipe post UI
    private ImageView recipeImage;
    private TextView recipeName;
    private ImageView save;
    private Context rContext;
    private List<Recipe> rPost;
    private TextView numLikes;
    private TextView numDislikes;

    private FirebaseUser firebaseUser;

    // Constructor for Reciper Adapter which takes in a list of Recipe objects and a context object
    // Context describes current state of application
    public RecipeAdapter(Context rContext, List<Recipe> rPost){
        this.rContext = rContext;
        this.rPost = rPost;
    }


    // Inflates the layout of the recipe post
    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(rContext).inflate(R.layout.post, parent, false);
        return new RecipeAdapter.viewHolder(view);
    }


    // Initializes recipe with specific recipe position and the correct image and recipe name
    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Recipe recipe = rPost.get(position);

        if(recipe.getURL() != "") {
            Picasso.with(rContext).load(recipe.getURL()).into(recipeImage);
        }
        else{
            Picasso.with(rContext).load("drawable://" + R.drawable.logo).into(recipeImage);
        }
        recipeName.setText(recipe.getName());

        numLikes.setText(""+recipe.getLikes());
        numDislikes.setText(""+recipe.getDislikes());

        // go to recipe page when clicked on
        recipeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RecipeFragment recipeFragment = new RecipeFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("recipe", recipe);
                recipeFragment.setArguments(bundle);
                ((FragmentActivity) view.getContext()).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container_fragment, recipeFragment)
                        .commit();
            }
        });
    }

    // Returns size of the list of recipes
    @Override
    public int getItemCount() {
        return rPost.size();
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    // Describes all items that will be placed in each row of the RecyclerView
    public class viewHolder extends RecyclerView.ViewHolder{

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            recipeImage = itemView.findViewById(R.id.post_image);
            recipeName = itemView.findViewById(R.id.recipetitle);
            numLikes = itemView.findViewById(R.id.numLikes);
            numDislikes = itemView.findViewById(R.id.numDislikes);
            save = itemView.findViewById(R.id.save);
        }
    }
}
