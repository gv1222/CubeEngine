package de.cubeisland.engine.core.recipe.condition;

import java.util.List;

import org.bukkit.event.inventory.PrepareItemCraftEvent;

import de.cubeisland.engine.core.recipe.Recipe;
import org.apache.commons.lang.Validate;

public class ShapelessIngredients extends RecipeCondition
{
    private List<Ingredient> ingredients;

    protected ShapelessIngredients()
    {
        super(); // No perm. allowed. Ingredients are ABSOLUTELY NEEDED!
    }

    @Override
    protected boolean process(PrepareItemCraftEvent event, Recipe recipe)
    {

        event.getInventory().getMatrix()
    }

    public ShapelessIngredients addIngredient(Ingredient ingredient)
    {
        Validate.isTrue(ingredients.size() < 9, "Shapeless recipes cannot have more than 9 ingredients");
        ingredients.add(ingredient);
        return this;
    }
}
