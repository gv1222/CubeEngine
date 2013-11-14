package de.cubeisland.engine.core.recipe.ingredient.condition;

import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;

public class PermissionCondition extends IngredientCondition
{
    private final String perm;
    private final boolean need;

    public PermissionCondition(String perm, boolean need)
    {
        this.perm = perm;
        this.need = need;
    }

    @Override
    protected boolean check(Permissible permissible, ItemStack itemStack)
    {
        if (permissible.hasPermission(perm))
        {
            return need;
        }
        return !need;
    }
}
