package de.cubeisland.engine.core.recipe.condition;

/**
 * A crafting ingredient
 */
public abstract class Ingredient extends RecipeCondition
{
    protected Ingredient()
    {
        super(); // No perm. allowed. Ingredients are ABSOLUTELY NEEDED!
    }
    // Ingredient Conditions
    // - material
    // - data ranges
    // - data bit set (potions)
    // - amount
    // - itemname / itemlore
    // - leathercolor rgb
    // - bookitem title / author / pages
    // - firework / firework charge item
    // - skullowner

    // all condition /w possible perm req. for condition to be needed


    // Ingredient Result:
    // - default (reduce by amount used if newamount = 0 replace /w air)
    // - replace with ItemStack
    // - keep (no change)
    // - use (change|set durability)

    // /w percentages
}
