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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;
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
import de.cubeisland.engine.core.util.Triplet;

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
                            Triplet<ItemStack[], Boolean, ItemStack[]> pair = this.shiftCrafting.get(humanEntity);
                            if (pair != null && pair.getSecond() != null)
                            {
                                if (recipe.ingredients instanceof ShapelessIngredients)
                                {
                                    pair.setSecond(!pair.getSecond());
                                    if (!pair.getSecond())
                                    {
                                        System.out.print("Shift-Craft step");
                                        ItemStack[] myMatrix = pair.getFirst();
                                        if (!this.reduceMyMatrix(myMatrix, recipe, (Player)humanEntity, null))// TODO block
                                        {
                                            pair.setSecond(null);
                                            event.getInventory().setResult(null); // Stop crafting!
                                            return;
                                        }
                                        event.getInventory().setResult(recipe.getResult((Player)humanEntity, null)); // TODO block
                                        return;
                                    }
                                    else System.out.print("Shift-Craft prep step");
                                }
                                else if (recipe.ingredients instanceof ShapedIngredients)
                                {
                                    ItemStack[] myMatrix = pair.getFirst();
                                    ItemStack[] nextCraftMatrix = pair.getThird();
                                    if (Arrays.equals(nextCraftMatrix, matrix))
                                    {
                                        this.reduceMatrix(nextCraftMatrix);
                                        if (!this.reduceMyMatrix(myMatrix, recipe, (Player)humanEntity, null))// TODO block
                                        {
                                            pair.setSecond(null);
                                            event.getInventory().setResult(null); // Stop crafting!
                                            return;
                                        }
                                        event.getInventory().setResult(recipe.getResult((Player)humanEntity, null)); // TODO block
                                        return;
                                    }
                                    // else preview (one of A LOT)
                                }
                            }
                            event.getInventory().setResult(recipe.getPreview((Player)humanEntity, null)); // TODO block
                            return;
                        }
                        else
                        {
                            System.out.print("No more match!");
                            event.getInventory().setResult(null);
                        }
                    }
                }
            }
        }
    }

    private ItemStack[] reduceMatrix(ItemStack[] matrix)
    {
        for (ItemStack item : matrix) // reduce
        {
            if (item != null)
            {
                int amount = item.getAmount() - 1;
                item.setAmount(amount < 0 ? 0 : amount);
            }
        }
        return matrix;
    }

    private boolean reduceMyMatrix(ItemStack[] matrix, WorkbenchRecipe recipe, Player player, BlockState block)
    {
        try
        {
            this.reduceMatrix(matrix);
            Map<Integer, ItemStack> ingredientResults = recipe.getIngredientResults(player, block, matrix);
            for (Entry<Integer, ItemStack> entry : ingredientResults.entrySet())
            {
                if (entry.getValue() != null)
                {
                    matrix[entry.getKey()] = entry.getValue();
                }
            }
        }
        catch (InvalidIngredientsException e) // TODO own exception for this
        {
            System.out.print("STOP Shift CRAFT");
            return false;
        }
        return true;
    }

    private Map<Player, Triplet<ItemStack[], Boolean, ItemStack[]>> shiftCrafting = new HashMap<>();

    private ItemStack[] deepClone(ItemStack[] matrix)
    {
        ItemStack[] clone = matrix.clone();
        for (int i = 0 ; i < clone.length ; i++)
        {
            if (clone[i] != null)
            {
                clone[i] = clone[i].clone();
            }
        }
        return clone;
    }

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
                final ItemStack[] matrix = this.deepClone(event.getInventory().getMatrix());
                if (event.getAction() == MOVE_TO_OTHER_INVENTORY)
                {
                    ItemStack[] startMatrix = recipe.ingredients instanceof ShapedIngredients ? deepClone(matrix) : null;
                    this.shiftCrafting.put(player, new Triplet<>(matrix, false, this.reduceMatrix(startMatrix)));
                }
                event.getInventory().setResult(recipe.getResult(player, null)); // TODO block
                final Map<Integer, ItemStack> ingredientResults = recipe.getIngredientResults(player, null, event.getInventory().getMatrix()); // TODO block
                if (!ingredientResults.isEmpty() || event.getAction() == MOVE_TO_OTHER_INVENTORY)
                {
                    recipe.runEffects(core, player);
                    final CraftingInventory inventory = event.getInventory();
                    core.getTaskManager().runTaskDelayed(core.getModuleManager().getCoreModule(),
                                 new Runnable()
                                 {
                                     @Override
                                     public void run()
                                     {
                                         Triplet<ItemStack[], Boolean, ItemStack[]> shift = shiftCrafting.remove(player);
                                         if (shift == null)
                                         {
                                             System.out.print("Normal Craft");
                                             reduceMyMatrix(matrix, recipe, player, null); // TODO block
                                             inventory.setMatrix(matrix);
                                         }
                                         else
                                         {
                                             inventory.setMatrix(reduceMatrix(shift.getFirst()));
                                         }
                                     }
                                 }, 1L);
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
