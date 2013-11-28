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

import java.util.Map;

import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import de.cubeisland.engine.core.recipe.condition.logic.Condition;
import de.cubeisland.engine.core.recipe.result.logic.Result;

public class WorkbenchRecipe extends Recipe<WorkbenchIngredients>
{
    private Result preview;

    public WorkbenchRecipe(WorkbenchIngredients ingredients, Result result)
    {
        super(ingredients, result);
    }

    public final Map<Integer, ItemStack> getIngredientResults(Player player, BlockState block, ItemStack[] matrix)
    {
        return ingredients.getIngredientResults(player, block, matrix.clone());
    }

    public final WorkbenchRecipe withPreview(Result preview)
    {
        this.preview = preview;
        return this;
    }

    @Override
    public final WorkbenchRecipe withCondition(Condition condition)
    {
        return (WorkbenchRecipe)super.withCondition(condition);
    }

    public final boolean matchesConditions(Player player, ItemStack[] matrix)
    {
        if (this.condition != null)
        {
            if (!this.condition.check(player, null))
            {
                return false;
            }
        }
        return ingredients.check(player, matrix);
    }

    public final ItemStack getPreview(Player player, BlockState block)
    {
        if (this.preview == null)
        {
            return this.getResult(player, block);
        }
        return this.preview.getResult(player, block, null);
    }

    public final boolean matchesRecipe(org.bukkit.inventory.Recipe checkRecipe)
    {
        for (org.bukkit.inventory.Recipe myRecipe : this.bukkitRecipes)
        {
            if (myRecipe.getClass().isAssignableFrom(checkRecipe.getClass())) // same type of recipe
            {
                if (checkRecipe.getResult().equals(myRecipe.getResult()))
                {
                    if (checkRecipe instanceof ShapelessRecipe)
                    {
                        if (((ShapelessRecipe)checkRecipe).getIngredientList().equals(
                            ((ShapelessRecipe)myRecipe).getIngredientList()))
                        {
                            return true;
                        }
                    }
                    else if (checkRecipe instanceof ShapedRecipe)
                    {
                        String[] checkShape = ((ShapedRecipe)checkRecipe).getShape();
                        String[] myShape = ((ShapedRecipe)myRecipe).getShape();
                        Map<Character, ItemStack> checkMap = ((ShapedRecipe)checkRecipe).getIngredientMap();
                        Map<Character, ItemStack> myMap = ((ShapedRecipe)myRecipe).getIngredientMap();
                        try
                        {
                            for (int i = 0; i < checkShape.length; i++)
                            {
                                for (int j = 0; j < checkShape[i].length(); j++)
                                {
                                    ItemStack checkItem = checkMap.get(checkShape[i].charAt(j));
                                    ItemStack myItem = myMap.get(myShape[i].charAt(j));
                                    if (checkItem != myItem)
                                    {
                                        if (checkItem == null || !checkItem.getData().equals(myItem.getData()))
                                        {
                                            break;
                                        }
                                    }
                                }
                            }
                            return true;
                        }
                        catch (IndexOutOfBoundsException ignore) // wrong shape
                        {}
                    }
                }
            }
        }
        return false;
    }

    // TODO possibility to prevent shift-crafting
}
