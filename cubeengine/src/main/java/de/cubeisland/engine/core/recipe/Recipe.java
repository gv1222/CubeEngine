package de.cubeisland.engine.core.recipe;

import org.bukkit.inventory.ItemStack;

/**
 * Represents some type of crafting recipe.
 */
public interface Recipe
{

    /**
     * Get the result of this recipe.
     *
     * @return The result stack
     */
    ItemStack getResult();
}
