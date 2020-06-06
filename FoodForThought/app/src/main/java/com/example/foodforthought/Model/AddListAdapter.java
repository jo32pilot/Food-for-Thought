/**
 * File containing the functionality that allows items to be added to the lists in the
 * AddRecipeFragment.
 *
 * @author John Li
 */
package com.example.foodforthought.Model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;
import com.example.foodforthought.R;
import java.util.LinkedList;
import java.util.List;

/**
 * Adapts an ingredient or instruction into a view that is used in the lists in the
 * AddRecipeFragment.
 */
public class AddListAdapter extends BaseAdapter implements ListAdapter {
    private List<String> list = new LinkedList<>();
    private Context context;

    /**
     * Constructor.
     * @param list The list being added to.
     * @param context The context the list is in.
     */
    public AddListAdapter(List<String> list, Context context) {
        this.list = list;
        this.context = context;
    }

    /**
     * Get the list's size.
     * @return The size of the list.
     */
    @Override
    public int getCount() {
        return list.size();
    }

    /**
     * Get a certain item in the list.
     * @param pos The position of the item in the list.
     * @return The item in the list.
     */
    @Override
    public Object getItem(int pos) {
        return list.get(pos);
    }

    /**
     * Defaults out.
     * @param pos Unused.
     * @return Always 0
     */
    @Override
    public long getItemId(int pos) {
        //just return 0 if your list items do not have an Id variable.
        return 0;
    }

    /**
     * Makes a view representing an item in the list.
     * @param position Position of the item in the list.
     * @param convertView The view being added to.
     * @param parent Unused
     * @return The altered view, now with the new item.
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;

        // null check
        if (view == null) {
            LayoutInflater inflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.custom_list_element, null);
        }

        // Handle TextView and display string from your list
        TextView listItemText = (TextView)view.findViewById(R.id.list_item_string);
        listItemText.setText(new StringBuilder().append(position + 1).append(". ")
                .append(list.get(position)).toString());

        // Handle buttons and add onClickListeners
        Button deleteBtn = (Button)view.findViewById(R.id.delete_btn);

        // the delete button functionality
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            /**
             * Removes this item from the list when the delete button is pressed.
             * @param v The view of the delete button
             */
            @Override
            public void onClick(View v) {
                // remove from list
                list.remove(position);

                // visual update
                notifyDataSetChanged();
            }
        });

        return view;
    }
}