package com.example.modloader;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class CustomRecipeRegistry {

    private final Plugin plugin;
    private final Logger logger;
    private final List<Recipe> registeredRecipes = new ArrayList<>();

    public CustomRecipeRegistry(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void register(Recipe recipe) {
        Bukkit.addRecipe(recipe);
        registeredRecipes.add(recipe);
        logger.info("Registered custom recipe: " + recipe.getResult().getType().name());
    }

    public void unregisterAll() {
        Iterator<Recipe> it = Bukkit.recipeIterator();
        while (it.hasNext()) {
            Recipe recipe = it.next();
            if (registeredRecipes.contains(recipe)) {
                it.remove();
                logger.info("Unregistered custom recipe: " + recipe.getResult().getType().name());
            }
        }
        registeredRecipes.clear();
    }
}
