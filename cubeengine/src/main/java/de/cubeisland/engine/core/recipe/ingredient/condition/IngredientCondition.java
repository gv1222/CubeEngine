/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.core.recipe.ingredient.condition;

import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;

public abstract class IngredientCondition
{
    private final String conditionPermission;
    private final boolean permIgnoreCondition;

    public AndCondition and(IngredientCondition condition)
    {
        return new AndCondition(this, condition);
    }

    public OrCondition or(IngredientCondition condition)
    {
        return new OrCondition(this, condition);
    }

    public NotCondition not()
    {
        return new NotCondition(this);
    }

    protected IngredientCondition(String perm, boolean permIgnores)
    {
        this.conditionPermission = perm;
        this.permIgnoreCondition = permIgnores;
    }

    protected IngredientCondition()
    {
        this(null, false);
    }

    public final boolean check(Permissible permissible, ItemStack itemStack)
    {
        if (needsCheck(permissible))
        {
            return process(permissible, itemStack);
        } // else condition is ignored
        return true;
    }

    protected abstract boolean process(Permissible permissible, ItemStack itemStack);

    private boolean needsCheck(Permissible permissible)
    {
        if (conditionPermission != null)
        {
            if (permissible.hasPermission(this.conditionPermission))
            {
                return !permIgnoreCondition;
            }
            else
            {
                return permIgnoreCondition;
            }
        }
        return true;
    }
}
