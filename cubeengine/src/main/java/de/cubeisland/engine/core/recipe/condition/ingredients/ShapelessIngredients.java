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
package de.cubeisland.engine.core.recipe.condition.ingredients;

import java.util.List;

import org.bukkit.inventory.ItemStack;

import org.apache.commons.lang.Validate;

public class ShapelessIngredients
{
    private List<Ingredient> ingredients;

    protected ShapelessIngredients(Ingredient... ingredients)
    {
        super(); // No perm. allowed. Ingredients are ABSOLUTELY NEEDED!
    }

    protected final boolean check(ItemStack[] matrix)
    {
        for (Ingredient ingredient : ingredients)
        {
            int index = ingredient.find(matrix);
            if (index == -1)
            {
                return false;
            }
            matrix[index] = null;
        }
        return true;
    }

    public final ShapelessIngredients addIngredient(Ingredient ingredient)
    {
        Validate.isTrue(ingredients.size() < 9, "Shapeless recipes cannot have more than 9 ingredients");
        ingredients.add(ingredient);
        return this;
    }
}
