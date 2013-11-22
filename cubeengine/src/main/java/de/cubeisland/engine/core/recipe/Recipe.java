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
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.recipe.condition.ingredient.MaterialProvider;
import de.cubeisland.engine.core.recipe.effect.RecipeEffect;
import de.cubeisland.engine.core.recipe.result.IngredientResult;

/**
 * Represents some type of crafting recipe.
 */
public class Recipe
{
    private Ingredients ingredients;
    private IngredientResult result;
    private RecipeEffect effect;
    private IngredientResult preview;

    public Recipe(Ingredients ingredients, IngredientResult result)
    {
        this.ingredients = ingredients;
        this.result = result;
    }

    public Recipe withPreview(IngredientResult preview)
    {
        this.preview = preview;
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

    public boolean matchesConditions(Player player, ItemStack[] matrix)
    {
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

    // TODO possibility to prevent shift-crafting
}
