/**
 * File that contains the functionality for the autocomplete view in the AddRecipeFragment.
 *
 * @author John Li
 */
package com.example.foodforthought.Model;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Acts as the visual representation of the list of autocompleted ingredients in the
 * AddRecipeFragment. Works closely with CustomAutoCompleteTextChangedListener.
 */
public class CustomAutoCompleteView extends androidx.appcompat.widget.AppCompatAutoCompleteTextView {
    /**
     * Constructor
     * @param context supers it
     */
    public CustomAutoCompleteView(Context context) {
        super(context);
    }

    /**
     * Constructor
     * @param context supers it
     * @param attrs supers it
     */
    public CustomAutoCompleteView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Constructor
     * @param context supers it
     * @param attrs supers it
     * @param defStyle supers it
     */
    public CustomAutoCompleteView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * This is how to disable AutoCompleteTextView filter.
     * @param text unused
     * @param keyCode supers it
     */
    @Override
    protected void performFiltering(final CharSequence text, final int keyCode) {
        String filterText = "";
        super.performFiltering(filterText, keyCode);
    }

    /**
     * after a selection we have to capture the new value and append to the existing text
     * @param text supers it
     */
    @Override
    protected void replaceText(final CharSequence text) {
        super.replaceText(text);
    }
}