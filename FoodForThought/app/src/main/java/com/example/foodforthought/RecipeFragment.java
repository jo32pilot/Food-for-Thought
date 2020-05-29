package com.example.foodforthought;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.example.foodforthought.Misc.Recipe;
import com.google.api.Distribution;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecipeFragment extends Fragment {

    private String userInput = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe, container, false);

        // deserialize recipe information
        Bundle bundle = getArguments();
        Recipe recipe = (Recipe) bundle.getSerializable("recipe");

        // POPULATE RECIPE INFORMATION:

        // page title
        TextView recipePageTitle = view.findViewById(R.id.recipePageTitle);
        recipePageTitle.setText(recipe.getName());

        // image
        ImageView recipeImage = view.findViewById(R.id.recipeImage);
        Picasso.with(getContext()).load(recipe.getURL()).into(recipeImage);

        // recipe name
        TextView recipeName = view.findViewById(R.id.recipeName);
        recipeName.setText(recipe.getName());

        // author field not implemented
        TextView author = view.findViewById(R.id.recipeAuthor);
        author.setText("By: placeholder author");

        // servings
        TextView servingSize = view.findViewById(R.id.servingSize);
        servingSize.setText(recipe.getYield());

        // description not implemented
        TextView description = view.findViewById(R.id.recipeDescription);
        description.setText("placeholder description");

        // ingredients
        LinearLayout recipeIngredients = view.findViewById(R.id.recipeIngredients);
        ArrayList<Map<String, String>> ingredients = recipe.getIngredients();
        for(int i = 0; i < ingredients.size(); i++) {
            // create a new checkbox
            CheckBox checkBox = new CheckBox(view.getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            checkBox.setLayoutParams(params);
            checkBox.setText(ingredients.get(i).get("ingredient"));
            recipeIngredients.addView(checkBox);
        }

        // instructions
        LinearLayout recipeInstructions = view.findViewById(R.id.recipeInstructions);
        ArrayList<String> instructions = recipe.getInstructions();
        for(int i = 0; i < instructions.size(); i++) {
            if(instructions.get(i) == "") {
                continue;
            }

            LinearLayout instruction = new LinearLayout(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            instruction.setLayoutParams(params);

            TextView step = new TextView(getContext());
            LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            step.setLayoutParams(params2);
            step.setText("Step " + (i+1) + ") ");
            step.setTextColor(Color.parseColor("#FD999A"));

            TextView infoText = new TextView(getContext());
            LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            infoText.setLayoutParams(params3);
            infoText.setText(instructions.get(i));

            instruction.addView(step);
            instruction.addView(infoText);

            recipeInstructions.addView(instruction);
        }

        // back button
        ImageButton backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.container_fragment, new MainFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        // comment feature
        ImageButton addCommentButton = view.findViewById(R.id.addCommentButton);
        addCommentButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Add Comment:");

                // Set up the input
                final EditText input = new EditText(view.getContext());
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // get user input
                        userInput = input.getText().toString();

                        // get comment section view
                        LinearLayout commentSection = view.findViewById(R.id.recipeComments);

                        // create new comment
                        LinearLayout newComment = new LinearLayout(view.getContext());
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.MATCH_PARENT);
                        newComment.setLayoutParams(params);

                        // create user profile
                        ImageView userProfile = new ImageView(view.getContext());
                        userProfile.setImageResource(R.drawable.profilepic);
                        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(150, 150);
                        userProfile.setLayoutParams(imageParams);

                        // create user comment text
                        TextView userText = new TextView((view.getContext()));
                        userText.setText(userInput);

                        // add user comment text and user profile to the comment
                        newComment.addView(userProfile);
                        newComment.addView(userText);

                        // add the comment to the comment section
                        commentSection.addView(newComment);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                // show the alert dialog box
                builder.show();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
