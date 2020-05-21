package com.example.foodforthought;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
// Fragment to load the addrecipe fragment
public class AddRecipeFragment extends Fragment {
    @Nullable
    @Override
    // Initializes view object with the layout associated with add recipe
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_addrecipe, container, false);
        return view;
    }
    // Method to initialize subclasses
    // Future implementation will include onClickListeners to navigate between fragments in other ways
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
