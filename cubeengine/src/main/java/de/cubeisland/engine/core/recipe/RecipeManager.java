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
import java.util.Set;

import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.PrepareItemCraftEvent;

import de.cubeisland.engine.core.module.Module;

public class RecipeManager
{
    private Map<Module, Set<Recipe>> recipes = new HashMap<>();

    public boolean registerRecipe(Module module, Recipe recipe)
    {
        // TODO search for conflicts
        // TODO regiter bukkit-recipes
         return this.getRecipes(module).add(recipe);
    }

    public boolean unregisterRecipe(Module module, Recipe recipe)
    {
        return this.getRecipes(module).remove(recipe);
    }

    public void unregisterAllRecipes(Module module)
    {
        this.recipes.remove(module);
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
        //TODO match recipe & call their methods
    }
}
