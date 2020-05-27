package com.example.foodforthought.Misc;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Class to store the number of matching queried ingredients a recipe has.
 */
public class Recipe{
    private String id;
    private Map<String, Object> recipe;
    private double matches;
    private String img;
    private String name;

    /**
     * Initializes number of queried ingredients that the recipe contains, as well as other
     * instance variables.
     * @param id Database Id of the recipe
     * @param recipe A mapping of the details of the recipe.
     * @param toMatch A list of the queried ingredients.
     */
    public Recipe(String id, Map<String, Object> recipe, List<String> toMatch){
        this.id = id;
        this.recipe = recipe;
        this.img = (String) recipe.get("image");
        this.name = (String) recipe.get("name");
        double countMatches = 0;


        // Stick all of the recipe's ingredients into a HashSet for O(1) lookup.
        HashSet<String> allIngredients =
                new HashSet<>((List<String>) recipe.get("all_ingredients"));


        // For each query ingredient
        for(String queryIngredient : toMatch){
            // if in HashSet, then this recipe contains an ingredient in the user's inventory.
            if(allIngredients.contains(queryIngredient)){
                countMatches++;
            }
        }

        // Compute ratio of num query ingredients in recipe to num ingredients in recipe.
        this.matches = countMatches / (double) allIngredients.size();

    }

    /**
     * Returns the recipe id.
     * @return the recipe id.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the recipe name.
     * @return the recipe name.
     */
    public String getName() {
        return name;
    }


    /**
     * Returns the recipe URL
     * @return the recipe URL
     */
    public String getURL(){


        return img;
    }

    /**
     * Returns the recipe info.
     * @return the recipe info.
     */
    public Map<String, Object> getRecipe() {
        return recipe;
    }

    /**
     * The ratio of how many query ingredients are in the recipe.
     * @return the ratio of how many query ingredients are in the recipe.
     */
    public double getMatches() {
        return matches;
    }
}