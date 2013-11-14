package de.cubeisland.engine.core.recipe.ingredient.condition;

import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;

public class ItemNameCondition extends IngredientCondition
{
    private String name;

    public ItemNameCondition(String name)
    {
        this.name = name;
    }

    @Override
    protected boolean check(Permissible permissible, ItemStack itemStack)
    {
        if (itemStack.getItemMeta().hasDisplayName())
        {
            return name.equals(itemStack.getItemMeta().getDisplayName());
        }
        return false;
    }
}
