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
package de.cubeisland.engine.core.recipe.ingredients;

import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;

/**
 * A crafting ingredient
 */
public class Ingredient
{
    public int find(Permissible permissible, ItemStack[] matrix)
    {
        for (int i = 0; i < matrix.length; i++)
        {
            if (this.check(permissible, matrix[i]))
            {
                return i;
            }
        }
        return -1;
    }

    public boolean check(Permissible permissible, ItemStack itemStack)
    {
        // TODO implement me
        return false;
    }


    // Ingredient Conditions
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
