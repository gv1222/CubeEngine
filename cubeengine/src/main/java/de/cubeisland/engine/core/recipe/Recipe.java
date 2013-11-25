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

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.MaterialData;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.recipe.condition.ingredient.MaterialProvider;
import de.cubeisland.engine.core.recipe.condition.logic.Condition;
import de.cubeisland.engine.core.recipe.effect.logic.Effect;
import de.cubeisland.engine.core.recipe.result.logic.Result;

/**
 * Represents some type of crafting recipe.
 */
public class Recipe
{
    private Ingredients ingredients;
    private Condition condition;
    private Result result;
    private Effect effect;
    private Result preview;

    public Recipe(Ingredients ingredients, Result result)
    {
        this.ingredients = ingredients;
        this.result = result;
    }

    public Recipe withPreview(Result preview)
    {
        this.preview = preview;
        return this;
    }

    public Recipe withCondition(Condition condition)
    {
        this.condition = condition;
        return this;
    }

    private Set<org.bukkit.inventory.Recipe> bukkitRecipes;

    public void registerBukkitRecipes(Server server)
    {
        bukkitRecipes = ingredients.getBukkitRecipes(this.getResultMaterial());
        for (org.bukkit.inventory.Recipe recipe : bukkitRecipes)
        {
            server.addRecipe(recipe);
        }
    }

    private MaterialData getResultMaterial()
    {
        if (result instanceof MaterialProvider)
        {
            LinkedList<MaterialData> materials = ((MaterialProvider)result).getMaterials(new LinkedList<MaterialData>());
            if (!materials.isEmpty())
            {
                return materials.iterator().next();
            }
        }
        throw new IllegalStateException("Recipe has no Material as Result");
    }

    public boolean matchesConditions(Player player, ItemStack[] matrix)
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

    public ItemStack getResult(Player player)
    {
        return result.getResult(player, null);
    }

    public Map<Integer, ItemStack> getIngredientResults(Player player, ItemStack[] matrix)
    {
        return ingredients.getIngredientResults(player, matrix.clone());
    }

    public void runEffects(Core core, Player player)
    {
        if (effect == null)
        {
            return;
        }
        this.effect.runEffect(core, player);
    }

    public ItemStack getPreview(Player player)
    {
        if (this.preview == null)
        {
            return this.getResult(player);
        }
        return this.preview.getResult(player, null);
    }

    public boolean matchesRecipe(org.bukkit.inventory.Recipe checkRecipe)
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
                                        if (checkItem == null || !checkItem.equals(myItem))
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
                    else if (checkRecipe instanceof FurnaceRecipe)
                    {
                        // TODO not implemented yet
                    }
                }
            }
        }
        return false;
    }

    // TODO possibility to prevent shift-crafting
}
