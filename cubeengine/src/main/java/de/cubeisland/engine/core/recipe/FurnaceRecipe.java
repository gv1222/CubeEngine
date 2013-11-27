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
package de.cubeisland.engine.core.recipe;

import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.recipe.condition.logic.Condition;
import de.cubeisland.engine.core.recipe.result.logic.Result;

public class FurnaceRecipe extends Recipe<FurnaceIngredients>
{
    public FurnaceRecipe(FurnaceIngredients ingredients, Result result)
    {
        super(ingredients, result);
    }

    @Override
    public final FurnaceRecipe withCondition(Condition condition)
    {
        return (FurnaceRecipe)super.withCondition(condition);
    }

    public boolean matchesRecipe(ItemStack smelting)
    {
        for (org.bukkit.inventory.Recipe recipe : this.bukkitRecipes)
        {
            if (recipe instanceof org.bukkit.inventory.FurnaceRecipe)
            {
                if (((org.bukkit.inventory.FurnaceRecipe)recipe).getInput().getData().
                    equals(smelting.getData()))
                {
                    return true;
                }
            }
        }
        return false;
    }
}
