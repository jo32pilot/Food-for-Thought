package com.example.foodforthought.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Class to store the number of matching queried ingredients a recipe has.
 */
public class Recipe implements Serializable {
    private String id;
    private Map<String, Object> recipe;
    private double matches;
    private String img;
    private String name;
    private String servingSize;
    private ArrayList<Map<String, Object>> ingredients;
    private ArrayList<String> instructions;
    private String author;
    private ArrayList<String> allIngredients;
    private long time;
    private ArrayList<Map<String, String>> comments;
    private long likes;
    private long dislikes;

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

        if(this.img.equals("")) {
            this.img = "https://i.ibb.co/8cYfmmk/logo.png";
        }

        this.name = (String) recipe.get("name");
        double countMatches = 0;

        // Stick all of the recipe's ingredients into a HashSet for O(1) lookup.
        HashSet<String> allIngredients =
                new HashSet<>((List<String>) recipe.get("all_ingredients"));

        if(toMatch != null) {
            // For each query ingredient
            for (String queryIngredient : toMatch) {
                // if in HashSet, then this recipe contains an ingredient in the user's inventory.
                if (allIngredients.contains(queryIngredient)) {
                    countMatches++;
                }
            }
        }

        // Compute ratio of num query ingredients in recipe to num ingredients in recipe.
        this.matches = countMatches / (double) allIngredients.size();

        this.servingSize = (String) recipe.get("yield");

        this.ingredients = (ArrayList<Map<String, Object>>) recipe.get("ingredients");

        this.instructions = (ArrayList<String>) recipe.get("instructions");

        this.author = (String)recipe.get("user_created");

        this.allIngredients = (ArrayList<String>) recipe.get("all_ingredients");

        this.time = (long)recipe.get("total_time");

        this.comments = (ArrayList<Map<String, String>>) recipe.get("comments");
        if (this.comments == null) {
            this.comments = new ArrayList<Map<String, String>> ();
        }

        if(recipe.get("likes") != null)
            this.likes = (long)recipe.get("likes");
        else
            this.likes = 0;

        if(recipe.get("dislikes") != null)
            this.dislikes = (long)recipe.get("dislikes");
        else
            this.dislikes = 0;
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

    /**
     *
     */
    public String getYield() { return servingSize; }

    /**
     *
     */
    public ArrayList<Map<String, Object>> getIngredients() { return ingredients; }

    /**
     *
     */
    public ArrayList<String> getInstructions() { return instructions; }

    /**
     *
     */
    public String getAuthor() { return author; }

    public ArrayList<String> getAllIngredients() { return allIngredients; }

    public long getTime() { return time; }

    public ArrayList<Map<String, String>> getComments() { return comments; }

    public long getLikes() { return likes; }

    public long getDislikes() { return dislikes; }

    public void setLikes(long likes) {
        this.likes = likes;
    }

    public void setDislikes(long dislikes) {
        this.dislikes = dislikes;
    }
}