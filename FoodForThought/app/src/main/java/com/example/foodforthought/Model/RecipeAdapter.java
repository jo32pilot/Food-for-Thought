/**
 * File that adapts a recipe in the database into a viewable post in a feed.
 */
package com.example.foodforthought.Model;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.foodforthought.R;
import com.example.foodforthought.Controller.RecipeFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;
import java.util.List;

/**
 * Adapter class for the Recipe class
 * Objective: Convert a recipe object at a certain position into an item to be inserted
 * into the RecyclerView
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.viewHolder> {
    // Objects of recipe post UI
    private ImageView recipeImage;
    private TextView recipeName;
    private Context rContext;
    private List<Recipe> rPost;
    private TextView numLikes;
    private TextView numDislikes;
    private FirebaseUser firebaseUser;

    // if the adapter was used in mainFeed, savedList, or profile
    private String origin;

    /**
     * Constructor for Reciper Adapter which takes in a list of Recipe objects and a context object
     * @param rContext Context describes current state of application
     * @param rPost list of recipes
     * @param origin where the adapter was originally used
     */
    public RecipeAdapter(Context rContext, List<Recipe> rPost, String origin) {
        this.rContext = rContext;
        this.rPost = rPost;
        this.origin = origin;
    }

    /**
     * Inflates the layout of the recipe post
     * @param parent Used in the inflate.
     * @param viewType Unused
     * @return The new viewHolder.
     */
    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(rContext).inflate(R.layout.post, parent, false);
        return new RecipeAdapter.viewHolder(view);
    }

    /**
     * Initializes recipe with specific recipe position and the correct image and recipe name.
     * @param holder The holder of the new post.
     * @param position The position in the list of the recipe.
     */
    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Recipe recipe = rPost.get(position);

        // null check...adds recipe image to teh new post
        if(recipe.getURL() != "") {
            Picasso.with(rContext).load(recipe.getURL()).into(recipeImage);
        }
        else{
            Picasso.with(rContext).load("drawable://" + R.drawable.logo).into(recipeImage);
        }

        // sets fields on the post
        recipeName.setText(recipe.getName());
        numLikes.setText(""+recipe.getLikes());
        numDislikes.setText(""+recipe.getDislikes());

        // when the image is clicked
        recipeImage.setOnClickListener(new View.OnClickListener() {
            /**
             * Goes to the recipe fragment.
             * @param view View of the image button.
             */
            @Override
            public void onClick(View view) {
                // send recipe data in a bundle to the recipe fragment
                RecipeFragment recipeFragment = new RecipeFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("recipe", recipe);

                bundle.putString("origin", origin);

                recipeFragment.setArguments(bundle);

                // switch screens
                ((FragmentActivity) view.getContext()).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container_fragment, recipeFragment)
                        .commit();
            }
        });

        // when the name is clicked
        recipeName.setOnClickListener(new View.OnClickListener() {
            /**
             * Goes to the recipe fragment.
             * @param view View of the text button.
             */
            @Override
            public void onClick(View view) {
                // send recipe data in a bundle to the recipe fragment
                RecipeFragment recipeFragment = new RecipeFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("recipe", recipe);
                bundle.putString("origin", origin);
                recipeFragment.setArguments(bundle);

                // switch screens
                ((FragmentActivity) view.getContext()).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container_fragment, recipeFragment)
                        .commit();
            }
        });
    }

    /**
     * Returns size of the list of recipes
     * @return Size of list.
     */
    @Override
    public int getItemCount() {
        return rPost.size();
    }

    /**
     * Defaults out.
     * @param position Simply returned back
     * @return The position.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Defaults out.
     * @param position Simply returned back
     * @return The position.
     */
    @Override
    public int getItemViewType(int position) {
        return position;
    }

    /**
     * Describes all items that will be placed in each row of the RecyclerView
     */
    public class viewHolder extends RecyclerView.ViewHolder {
        /**
         * Constructor.
         * @param itemView View of the post.
         */
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.post_image);
            recipeName = itemView.findViewById(R.id.recipetitle);
            numLikes = itemView.findViewById(R.id.numLikes);
            numDislikes = itemView.findViewById(R.id.numDislikes);
        }
    }
}