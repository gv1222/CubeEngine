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
package de.cubeisland.engine.core.recipe.ingredient;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;

import de.cubeisland.engine.core.recipe.ingredient.condition.IngredientCondition;
import de.cubeisland.engine.core.recipe.ingredient.condition.MaterialCondition;
import de.cubeisland.engine.core.recipe.ingredient.condition.MaterialProvider;
import de.cubeisland.engine.core.recipe.ingredient.result.IngredientResult;

/**
 * A crafting ingredient
 */
public class Ingredient
{
    private IngredientCondition condition;
    private IngredientResult result;

    private Ingredient(IngredientCondition condition)
    {
        this.condition = condition;
    }

    public final int find(Permissible permissible, ItemStack[] matrix)
    {
        for (int i = 0; i < matrix.length; i++)
        {
            if (condition.check(permissible, matrix[i]))
            {
                return i;
            }
        }
        return -1;
    }

    public final boolean check(Permissible permissible, ItemStack itemStack)
    {
        return condition.check(permissible, itemStack);
    }

    /**
     * Returns the resulting itemStack
     * <p>will return null if no result is given -> use default behaviour
     *
     * @param permissible
     * @param itemStack
     * @return
     */
    public final ItemStack getResult(Permissible permissible, ItemStack itemStack)
    {
        if (result == null)
        {
            return null;
        }
        if (result.check(permissible, itemStack))
        {
            return result.getResult(permissible, itemStack);
        }
        return null;
    }

    public final Ingredient setResult(IngredientResult result)
    {
        this.result = result;
        return this;
    }

    /**
     * Creates a new Ingredient matching only the given material
     *
     * @param material
     * @return
     */
    public static Ingredient ofMaterial(Material material)
    {
        return new Ingredient(MaterialCondition.of(material));
    }

    /**
     * Creates a new Ingredient matching the given condition
     *
     * @param condition
     * @return
     */
    public static Ingredient ofCondition(IngredientCondition condition)
    {
        return new Ingredient(condition);
    }

    public final Set<Material> getMaterials()
    {
        if (condition instanceof MaterialProvider)
        {
            return ((MaterialProvider)condition).getMaterials(new HashSet<Material>());
        }
        throw new IllegalStateException("No Material given for ingredient!");
    }
}
