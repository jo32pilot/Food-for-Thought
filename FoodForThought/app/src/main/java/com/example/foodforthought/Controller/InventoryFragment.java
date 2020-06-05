/**
 * File hold functionality for the inventory page of the application.
 *
 * @author Ankur Duggal
 */
package com.example.foodforthought.Controller;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.foodforthought.Model.Database;
import com.example.foodforthought.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Controls the inventory page of the app. The inventory can have ingredients added to it,
 * removed from it. The ingredients in your inventory detemine what recipes appear in your
 * home feed.
 */
public class InventoryFragment extends Fragment {
    // constants
    public static final int ITEM_DPI = 250;
    public static final int TEXT_SIZE = 20;
    public static final int BUTTON_DPI = 40;
    public static final int AMOUNT_DPI = 30;

    // global views and variables
    private SearchView searchIngredients;
    private LinearLayout pantryListLayout;
    private Database db = new Database();
    private Map<String, Object> userInventory;
    private String userIngredientsId;
    private CollectionReference inventoryRef;
    private SimpleCursorAdapter mAdapter;
    private String[] SUGGESTIONS = new String[10];
    private float scale;

     /**
     * Builds the view when the fragment is opened.
     * @param inflater Inflated view to fit the screen.
     * @param container What the screen is contained in.
     * @param savedInstanceState Persists data throughout configuration changes.
     * @return The fully built view.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inventory, container, false);

        // searchbar and pantry list views
        searchIngredients = view.findViewById(R.id.searchInv);
        pantryListLayout = view.findViewById(R.id.pantryListLayout);

        // If user isn't logged in or has logged out.
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            getActivity().finish();
        }

        // Get User inventory id in database
        String uid = user.getUid();
        userIngredientsId = "user_ingredients_id_" + uid;

        // Get all ingredients already in database and set up screen
        db.getDocument("user_ingredients", userIngredientsId, onGetUserInventory);

        // Set up local storage of ingredients
        inventoryRef = db.getDB().collection("ingredients");

        // Set up Table for Cursor to go through to get suggestions
        final String[] from = new String[] {"ingredient"};
        final int[] to = new int[] {android.R.id.text1};
        mAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_1,
                null,
                from,
                to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        scale = Objects.requireNonNull(getContext()).getResources().getDisplayMetrics().density;

        return view;
    }

    /**
     * After the view is created, give it functionality. Adds search feature, plus/minus feature.
     * @param view The newly made view.
     * @param savedInstanceState Persists data throughout configuration changes.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup Suggestions
        searchIngredients.setSuggestionsAdapter(mAdapter);
        searchIngredients.setIconifiedByDefault(false);

        // handles the searchbar action listener
        searchIngredients.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            /**
             * Defaults out.
             * @param position Unused
             * @return Always false.
             */
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            /**
             * When suggestion is clicked update database and UI with new item to be added
             * @param position Postion clicked on.
             * @return Success or failure.
             */
            @Override
            public boolean onSuggestionClick(int position) {
                // Get Text from cursor to know which suggestion was clicked
                Cursor cursor = (Cursor) mAdapter.getItem(position);
                String txt = cursor.getString(cursor.getColumnIndex("ingredient"));
                if(txt != null) {
                    // If the item already exists in database/UI tell user
                    if(userInventory.containsKey(txt)){
                        Toast.makeText(InventoryFragment.this.getContext(),
                                "Ingredient already in pantry, please choose another.",
                                Toast.LENGTH_SHORT).show();
                    }
                    // If new ingredient put into database and display on UI
                    else {
                        userInventory.put(txt, 1);
                        createItem(txt, 1);
                        // Clear ingredient entry
                        searchIngredients.setQuery("", false);
                        return true;
                    }
                }
                return false;
            }
        });

        searchIngredients.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            /**
             * Not allowing users to put in their own custom ingredients,
             * they must click a suggestion.
             * @param query The name of the queries ingredient.
             * @return Success or failure.
             */
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(InventoryFragment.this.getContext(),
                        "Ingredient not found. Please click a suggestion from the list.",
                        Toast.LENGTH_LONG).show();
                return false;
            }

            /**
             * Fill in suggestions as text is changed.
             * @param newText The changed query text.
             * @return Success or failure.
             */
            @Override
            public boolean onQueryTextChange(String newText) {
                inventoryRef.whereGreaterThanOrEqualTo("name", newText)
                        .limit(10)
                        .get()
                        .addOnCompleteListener(onGetAllIngredients);
                return true;
            }
        });

    }

    /**
     * Helper method to populate the suggestions.
     */
    private void populateAdapter() {
        final MatrixCursor c = new MatrixCursor(new String[]{ BaseColumns._ID, "ingredient" });

        // Iterates through all possible suggestions and displays them
        for (int i=0; i<SUGGESTIONS.length; i++) {
            c.addRow(new Object[] {i, SUGGESTIONS[i]});
        }
        mAdapter.changeCursor(c);
    }

    // Populate Suggestions array to populate the adapter
    OnCompleteListener<QuerySnapshot> onGetAllIngredients = task -> {
        int i = 0;
        for(QueryDocumentSnapshot doc : task.getResult()){
            SUGGESTIONS[i] = doc.getId();
            i++;
        }
        populateAdapter();
    };

    // Listener for when we've received the user's ingredients.
    OnCompleteListener<DocumentSnapshot> onGetUserInventory = new OnCompleteListener<DocumentSnapshot>() {
        /**
         * When the user's inventory is gotten from the database.
         * @param task The user inventory data.
         */
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            if(task.isSuccessful()){
                DocumentSnapshot userIngredients = task.getResult();

                // get the Ingredients from database to store in local storage and set up UI
                if(userIngredients != null){
                    userInventory = (Map<String, Object>) userIngredients.get("inventory");
                    if(userInventory != null ){
                        setUpScreen();
                    }
                    // If no ingredients make a map object
                    else{
                        userInventory = new HashMap<>();
                    }
                }
            }
        }
    };

    /**
     * Helper method to setup the UI to show all items in database
     */
    protected void setUpScreen() {
        // Iterate through the local inventory and show them on screen
        for(String key : userInventory.keySet()){
            createItem(key, (long) userInventory.get(key));
        }
    }

    /**
     * Helper method to show item on screen and update database
     * @param name name of item to be added
     * @param number amount of the item to be added
     */
    private void createItem(String name, long number) {
        // Update database with new item
        Map<String, Object> updatedMap = new HashMap<>();
        updatedMap.put("inventory", userInventory);
        db.update("user_ingredients", userIngredientsId, updatedMap,
                this, "Could not create item. Please try again", onSuccessListener);

        // Create Row to put all items into
        LinearLayout linearLayout = new LinearLayout(this.getContext());
        linearLayout.setId(View.generateViewId());
        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        // Setup another layout to put everything in besides the main text
        // (to make it easy push to right of screen)
        LinearLayout amountLayout = new LinearLayout(this.getContext());
        amountLayout.setId(View.generateViewId());
        LinearLayout.LayoutParams amountLayoutParams =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
        amountLayoutParams.gravity = RelativeLayout.ALIGN_PARENT_END;
        amountLayout.setLayoutParams(amountLayoutParams);

        // Create all items of new row
        TextView item = new TextView(this.getContext());
        ImageButton minusButton = new ImageButton(this.getContext());
        ImageButton plusButton = new ImageButton(this.getContext());
        EditText amount = new EditText(this.getContext());

        // Set valid IDs
        item.setId(View.generateViewId());
        minusButton.setId(View.generateViewId());
        plusButton.setId(View.generateViewId());
        amount.setId(View.generateViewId());

        // Add items of new row into new row
        linearLayout.addView(item);
        amountLayout.addView(amount);
        amountLayout.addView(plusButton);
        amountLayout.addView(minusButton);
        linearLayout.addView(amountLayout);

        // Add row to list
        pantryListLayout.addView(linearLayout);

        // Set parameters for item
        int itemPixels = calculatePixels(ITEM_DPI);
        item.setLayoutParams(new LinearLayout.LayoutParams(
                itemPixels, ViewGroup.LayoutParams.WRAP_CONTENT));
        // Set text to ingredient
        item.setText(name);
        // Set size of text
        item.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE);
        item.setGravity(Gravity.BOTTOM);

        // Set image for minus button
        minusButton.setImageResource(R.drawable.ic_action_min);
        int buttonPixels = calculatePixels(BUTTON_DPI);
        minusButton.setLayoutParams(new LinearLayout.LayoutParams(
                buttonPixels, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Set image for plus button
        plusButton.setImageResource(R.drawable.ic_action_add);
        plusButton.setLayoutParams(new LinearLayout.LayoutParams(
                buttonPixels, ViewGroup.LayoutParams.WRAP_CONTENT));

         // Set regular distance for amount text (UI changes)
        int amountPixels = calculatePixels(AMOUNT_DPI);
        amount.setLayoutParams(new LinearLayout.LayoutParams(amountPixels, ViewGroup.LayoutParams.WRAP_CONTENT));
        amount.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE);
        amount.setGravity(Gravity.CENTER);

        // Set input type to numbers
        amount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);

        // Set base amount to input's value
        amount.setText(String.valueOf(number));
        amount.addTextChangedListener(new TextWatcher() {
            /**
             * Defaults out.
             * @param s Unused.
             * @param start Unused.
             * @param count Unused
             * @param after Unused.
             */
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            /**
             * Deaults out.
             * @param s Unused.
             * @param start Unused.
             * @param before Unused.
             * @param count Unused.
             */
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            /**
             * Once the text is changed, remove that ingredient in the database.
             * @param s The edited text.
             */
            @Override
            public void afterTextChanged(Editable s) {
                // Null pointer checker
                if (s == null) {}
                // Do nothing if string is empty
                else if (s.toString().isEmpty()) {}
                // If new value is 0 or less remove from database and UI
                else if (Integer.parseInt(s.toString()) <= 0) {
                    // Remove from database
                    userInventory.remove(name);
                    Map<String, Object> updatedMap = new HashMap<>();
                    updatedMap.put("inventory", userInventory);
                    db.update("user_ingredients", userIngredientsId, updatedMap,
                            InventoryFragment.this,
                            "Could not remove. Please try again", onSuccessListener);
                    // Remove from UI
                    pantryListLayout.removeView(linearLayout);
                }
                // If a valid integer, update amount in UI and in database
                else {
                    int temp = Integer.parseInt(s.toString());
                    Map<String, Object> map = new HashMap<>();
                    map.put("inventory." + name, temp);
                    db.update("user_ingredients", userIngredientsId,
                            map, InventoryFragment.this,
                            "Could not update amount. Please try again",
                            onSuccessListener);
                }
            }
        });

        // What to do if plus button is clicked
        plusButton.setOnClickListener(new View.OnClickListener() {
            /**
             * When the plus button is clicked, add one more of the ingredient
             * to the user's inventory.
             * @param v The plus button view.
             */
            @Override
            public void onClick(View v) {
                // Increment counter
                int temp = Integer.parseInt(amount.getText().toString());
                // Update UI
                amount.setText(String.valueOf(temp + 1));
                // Create map to update database
                Map<String, Object> map = new HashMap<>();
                // Update database with new amount
                map.put("inventory." + name, temp + 1);
                db.update("user_ingredients", userIngredientsId, map,
                        InventoryFragment.this,
                        "Could not update amount. Please try again",
                        onSuccessListener);
            }
        });

        // What to do if minus button is clicked
        minusButton.setOnClickListener(v -> {
            // Decrease counter
            int temp = Integer.parseInt(amount.getText().toString());
            // Create map to update database
            Map<String, Object> map = new HashMap<>();
            map.put("inventory." + name, temp - 1);
            // Update database with new amount
            db.update("user_ingredients", userIngredientsId, map,
                    InventoryFragment.this,
                    "Could not update amount. Please try again",
                    onSuccessListener);
            // Update UI
            amount.setText(String.valueOf(temp - 1));
        });
    }

    /**
     * Converts DPI to local pixel count
     * @param dpi Density independent pixels to be converted
     * @return dpi converted to local pixel count
     */
    private int calculatePixels(int dpi){
        int pixels = (int) (dpi * scale + 0.5f);
        return pixels;
    }

    // Empty Success Listener to ensure no output is printed out on success
    private OnSuccessListener<Void> onSuccessListener = aVoid -> {};
}