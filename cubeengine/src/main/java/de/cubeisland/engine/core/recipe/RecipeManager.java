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
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.module.Module;

import static org.bukkit.event.inventory.InventoryAction.MOVE_TO_OTHER_INVENTORY;

public class RecipeManager implements Listener
{
    protected Map<Module, Set<Recipe>> recipes = new HashMap<>();
    protected Set<WorkbenchRecipe> workbenchRecipes = new HashSet<>();
    protected Set<FurnaceRecipe> furnaceRecipes = new HashSet<>();
    private final FurnaceManager furnaceManager;

    protected Core core;

    public RecipeManager(Core core)
    {
        this.core = core;
        this.furnaceManager = new FurnaceManager(this);
    }

    public void registerRecipe(Module module, Recipe recipe)
    {
        recipe.registerBukkitRecipes(Bukkit.getServer());
        this.getRecipes(module).add(recipe);
        if (recipe instanceof WorkbenchRecipe)
        {
            this.workbenchRecipes.add((WorkbenchRecipe)recipe);
        }
        else if (recipe instanceof FurnaceRecipe)
        {
            this.furnaceRecipes.add((FurnaceRecipe)recipe);
        }
        else
        {
            throw new IllegalStateException();
        }
    }

    public void unregisterRecipe(Module module, Recipe recipe)
    {
        // TODO remove bukkit recipes (saved recipes are in our Recipe object)
        this.getRecipes(module).remove(recipe);
        this.workbenchRecipes.remove(recipe);
    }

    public void unregisterAllRecipes(Module module)
    {
        // TODO remove bukkit recipes (saved recipes are in our Recipe object)
        Set<Recipe> remove = this.recipes.remove(module);
        this.workbenchRecipes.removeAll(remove);
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
            if (humanEntity instanceof Player)
            {
                for (WorkbenchRecipe recipe : workbenchRecipes)
                {
                    if (recipe.matchesRecipe(event.getRecipe()))
                    {
                        if (recipe.matchesConditions((Player)humanEntity, matrix))
                        {
                            Boolean shiftCraft = isShiftCrafting.get(humanEntity);
                            if (shiftCraft != null)
                            {
                                isShiftCrafting.put((Player)humanEntity, !shiftCraft);
                                if (shiftCraft)
                                {
                                    ItemStack[] myMatrix = this.shiftCrafting.get(humanEntity);
                                    Map<Integer, ItemStack> ingredientResults = null;
                                    try
                                    {
                                        ingredientResults = recipe.getIngredientResults((Player)humanEntity, null, myMatrix); // TODO block
                                    }
                                    catch (IllegalStateException e) // TODO own exception for this
                                    {
                                        event.getInventory().setResult(null); // Stop crafting!
                                        return;
                                    }
                                    for (int i = 0 ; i < myMatrix.length ; i++)
                                    {
                                        if (myMatrix[i] == null)
                                        {
                                            continue;
                                        }
                                        int amount = myMatrix[i].getAmount() - 1;
                                        if (amount < 0)
                                        {
                                            amount = 0;
                                        }
                                        myMatrix[i].setAmount(amount);
                                    }
                                    for (Entry<Integer, ItemStack> entry : ingredientResults.entrySet())
                                    {
                                        myMatrix[entry.getKey()] = entry.getValue();
                                    }
                                    event.getInventory().setResult(recipe.getResult((Player)humanEntity, null)); // TODO block
                                    return;
                                }
                            }
                            event.getInventory().setResult(recipe.getPreview((Player)humanEntity, null)); // TODO block
                            return;
                        }
                        else
                        {
                            event.getInventory().setResult(null);
                        }
                    }
                }
            }
        }
    }

    private Map<Player, ItemStack[]> shiftCrafting = new HashMap<>();
    private Map<Player, Boolean> isShiftCrafting = new HashMap<>();

    @EventHandler
    public void onItemCraft(CraftItemEvent event)
    {
        if (!(event.getWhoClicked() instanceof Player) || event.getAction() == InventoryAction.NOTHING)
        {
            return;
        }
        final Player player = (Player)event.getWhoClicked();
        for (final WorkbenchRecipe recipe : workbenchRecipes)
        {
            if (recipe.matchesConditions(player, event.getInventory().getMatrix()))
            {
                final ItemStack[] matrix = event.getInventory().getMatrix();
                for (int i = 0 ; i < matrix.length ; i++)
                {
                    if (matrix[i] == null)
                    {
                        continue;
                    }
                    matrix[i] = matrix[i].clone();
                }
                if (event.getAction() == MOVE_TO_OTHER_INVENTORY)
                {
                    this.shiftCrafting.put(player, matrix);
                    this.isShiftCrafting.put(player, false);
                }
                event.getInventory().setResult(recipe.getResult(player, null)); // TODO block
                final Map<Integer, ItemStack> ingredientResults = recipe.getIngredientResults(player, null, event.getInventory().getMatrix()); // TODO block
                if (!ingredientResults.isEmpty() || event.getAction() == MOVE_TO_OTHER_INVENTORY)
                {
                    // TODO handle shift crafting when using percentages for results.
                    // result is reset in onPrepareItemCraft when doing this
                    // problem is ingredients. need to calculate of possible
                    // what when craft req more than 1 of each item?
                    // Action is then: MOVE_TO_OTHER_INVENTORY
                    // other possible PICKUP_ALL
                    // PICKUP_HALF <- does it really pickup half? no
                    // TODO handle if default result stackable but special result not cancel crafting or stmh else
                    recipe.runEffects(core, player);
                    final CraftingInventory inventory = event.getInventory();
                    core.getTaskManager().runTaskDelayed(core.getModuleManager().getCoreModule(),
                                 new Runnable()
                                 {
                                     @Override
                                     public void run()
                                     {
                                         ItemStack[] myMatrix = shiftCrafting.remove(player);
                                         if (myMatrix == null)
                                         {
                                             myMatrix = inventory.getMatrix();
                                         }
                                         Map<Integer, ItemStack> ingredientResults = recipe.getIngredientResults(player, null, matrix); // TODO block
                                         if (isShiftCrafting.remove(player) != null)
                                         {
                                             for (int i = 0 ; i < myMatrix.length ; i++)
                                             {
                                                 if (myMatrix[i] == null)
                                                 {
                                                     continue;
                                                 }
                                                 int amount = myMatrix[i].getAmount() - 1;
                                                 if (amount < 0)
                                                 {
                                                     amount = 0;
                                                 }
                                                 myMatrix[i].setAmount(amount);
                                             }
                                         }
                                         for (Entry<Integer, ItemStack> entry : ingredientResults.entrySet())
                                         {
                                             myMatrix[entry.getKey()] = entry.getValue();
                                         }
                                         inventory.setMatrix(myMatrix);
                                         // TODO try to set preview
                                     }
                                 }, 0L);
                }
                core.getTaskManager().runTaskDelayed(core.getModuleManager().getCoreModule(),
                     new Runnable()
                     {
                         @Override
                         public void run()
                         {
                             player.updateInventory();
                         }
                     }, 2L);

                return;
            }
        }
    }

     //TODO remove recipe when unloading module
}
