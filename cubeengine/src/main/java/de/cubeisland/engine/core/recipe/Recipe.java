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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;

import de.cubeisland.engine.core.recipe.ingredient.Ingredients;
import de.cubeisland.engine.core.recipe.ingredient.condition.MaterialProvider;
import de.cubeisland.engine.core.recipe.ingredient.result.IngredientResult;

/**
 * Represents some type of crafting recipe.
 */
public class Recipe
{
    private Ingredients ingredients;
    private IngredientResult result;
    private Map<Integer, ItemStack> ingredientResults;

    public Recipe(Ingredients ingredients, IngredientResult result)
    {
        this.ingredients = ingredients;
        this.result = result;
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

    private Material getResultMaterial()
    {
        if (result instanceof MaterialProvider)
        {
            Set<Material> materials = ((MaterialProvider)result).getMaterials(new HashSet<Material>());
            if (!materials.isEmpty())
            {
                return materials.iterator().next();
            }
        }
        throw new IllegalStateException("Recipe has no Material as Result");
    }

    public boolean matchesConditions(Permissible permissible, ItemStack[] matrix)
    {
        return ingredients.check(permissible, matrix);
    }

    public ItemStack getResult(Permissible permissible)
    {
        return result.getResult(permissible, null);
    }

    public Map<Integer, ItemStack> getIngredientResults(Permissible permissible, ItemStack[] matrix)
    {
        return ingredients.getIngredientResults(permissible, matrix);
    }
}
