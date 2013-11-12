package de.cubeisland.engine.core.recipe.condition;

import org.bukkit.event.inventory.PrepareItemCraftEvent;

import de.cubeisland.engine.core.recipe.Recipe;

public class ShapedIngredients extends RecipeCondition
{
    protected ShapedIngredients()
    {
        super(); // No perm. allowed. Ingredients are ABSOLUTELY NEEDED!
    }

    @Override
    protected boolean process(PrepareItemCraftEvent event, Recipe recipe)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
