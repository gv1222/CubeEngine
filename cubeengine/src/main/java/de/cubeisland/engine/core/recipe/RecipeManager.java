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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.module.Module;

public class RecipeManager implements Listener
{
    private Map<Module, Set<Recipe>> recipes = new HashMap<>();
    private Set<Recipe> allRecipes = new HashSet<>();
    private Core core;

    public RecipeManager(Core core)
    {
        this.core = core;
    }

    public void registerRecipe(Module module, Recipe recipe)
    {
        recipe.registerBukkitRecipes(Bukkit.getServer());
        this.getRecipes(module).add(recipe);
        this.allRecipes.add(recipe);
    }

    public void unregisterRecipe(Module module, Recipe recipe)
    {
        // TODO remove bukkit recipes (saved recipes are in our Recipe object)
        this.getRecipes(module).remove(recipe);
        this.allRecipes.remove(recipe);
    }

    public void unregisterAllRecipes(Module module)
    {
        // TODO remove bukkit recipes (saved recipes are in our Recipe object)
        Set<Recipe> remove = this.recipes.remove(module);
        this.allRecipes.removeAll(remove);
    }

    private Set<Recipe> getRecipes(Module module)
    {
        Set<Recipe> recipeSet = this.recipes.get(module);
        if (recipeSet == null)
        {
            recipeSet = new HashSet<>();
            this.recipes.put(module, recipeSet);
        }
        return recipeSet;
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event)
    {
        ItemStack[] matrix = event.getInventory().getMatrix();
        if (event.getViewers().size() > 1)
        {
            event.getInventory().setResult(null);
            CubeEngine.getLog().warn("Aborted PrepareItemCraftEvent because {} players were looking into the same CraftingInventory!", event.getViewers().size());
            return;
        }
        for (HumanEntity humanEntity : event.getViewers()) // only 1 humanEntity
        {
            for (Recipe recipe : allRecipes)
            {
                if (recipe.matchesConditions(humanEntity, matrix))
                {
                    event.getInventory().setResult(recipe.getResult(humanEntity));
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onItemCraft(CraftItemEvent event)
    {
        for (Recipe recipe : allRecipes)
        {
            if (recipe.matchesConditions(event.getWhoClicked(), event.getInventory().getMatrix()))
            {
                event.getInventory().setResult(recipe.getResult(event.getWhoClicked()));
                final Map<Integer, ItemStack> ingredientResults = recipe.getIngredientResults(event.getWhoClicked(), event.getInventory().getMatrix());
                if (!ingredientResults.isEmpty())
                {
                    final CraftingInventory inventory = event.getInventory();
                    core.getTaskManager().runTaskDelayed(core.getModuleManager().getCoreModule(),
                                 new Runnable()
                                 {
                                     @Override
                                     public void run()
                                     {
                                         ItemStack[] matrix = inventory.getMatrix();
                                         for (Entry<Integer, ItemStack> entry : ingredientResults.entrySet())
                                         {
                                             matrix[entry.getKey()] = entry.getValue();
                                         }
                                         inventory.setMatrix(matrix);
                                     }
                                 }, 0L);
                }
                if (event.getWhoClicked() instanceof Player)
                {
                    final Player whoClicked = (Player)event.getWhoClicked();
                    core.getTaskManager().runTaskDelayed(core.getModuleManager().getCoreModule(),
                                 new Runnable()
                                 {
                                     @Override
                                     public void run()
                                     {
                                         whoClicked.updateInventory();
                                     }
                                 }, 2L);
                }
                return;
            }
        }
    }
}
