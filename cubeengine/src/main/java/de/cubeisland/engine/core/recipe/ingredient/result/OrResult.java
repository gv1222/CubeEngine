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
package de.cubeisland.engine.core.recipe.ingredient.result;

import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;

public class OrResult extends IngredientResult
{
    private IngredientResult result1;
    private IngredientResult result2;

    public OrResult(IngredientResult result1, IngredientResult result2)
    {
        this.result1 = result1;
        this.result2 = result2;
    }

    @Override
    public ItemStack getResult(Permissible permissible, ItemStack itemStack)
    {
        if (result1.check(permissible, itemStack))
        {
            return result1.getResult(permissible, itemStack);
        }
        if (result2.check(permissible, itemStack))
        {
            return result2.getResult(permissible, itemStack);
        }
        return null;
    }
}
