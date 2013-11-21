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
    public final AndCondition and(IngredientCondition condition)
    {
        return new AndCondition(this, condition);
    }

    public final OrCondition or(IngredientCondition condition)
    {
        return new OrCondition(this, condition);
    }

    public final NotCondition not()
    {
        return new NotCondition(this);
    }

    public final IngredientCondition perm(String perm, boolean need)
    {
        return this.and(new PermissionCondition(perm, need));
    }

    public abstract boolean check(Permissible permissible, ItemStack itemStack);

    // Ingredient Conditions
    // - itemname / itemlore
    // - leathercolor rgb
    // - bookitem title / author / pages
    // - firework / firework charge item
    // - skullowner

}
