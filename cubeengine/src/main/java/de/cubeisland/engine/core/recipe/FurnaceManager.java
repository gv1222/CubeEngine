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
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Furnace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.module.CoreModule;
import de.cubeisland.engine.core.util.Pair;
import de.cubeisland.engine.core.util.Profiler;

import static de.cubeisland.engine.core.recipe.FuelIngredient.DEFAULT_SMELT_TIME;

public class FurnaceManager implements Listener
{
    protected final CoreModule coreModule;
    protected RecipeManager manager;

    protected Map<Location, ItemStack> fuelMap = new HashMap<>();
    protected Map<Location, CustomSmelting> smeltMap = new HashMap<>();

    public FurnaceManager(RecipeManager manager)
    {
        this.manager = manager;
        this.coreModule = this.manager.core.getModuleManager().getCoreModule();
        this.manager.core.getEventManager().registerListener(coreModule, this);
    }

    @EventHandler
    public void onBurn(FurnaceBurnEvent event)
    {
        if (event.getBlock().getState() instanceof Furnace)
        {
            final Location location = event.getBlock().getLocation();
            final Furnace furnace = (Furnace)event.getBlock().getState();
            fuelMap.remove(location);
            boolean invalidRecipe = false;
            for (final FurnaceRecipe recipe : this.manager.furnaceRecipes)
            {
                FurnaceIngredients ingredients = recipe.getIngredients();
                if (recipe.matchesRecipe(furnace.getInventory().getSmelting()))
                {
                    Pair<Integer,Integer> times = ingredients.getTimes(event.getFuel(), furnace.getInventory().getSmelting());
                    if (times != null)
                    {
                        if (times.getLeft() != 0)
                        {
                            event.setBurnTime(times.getLeft());
                            this.manager.core.getTaskManager().runTaskDelayed(coreModule, new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    fuelEnd(furnace, location);
                                }
                            }, times.getLeft() - 1);
                            fuelMap.put(location, event.getFuel());
                            System.out.print("new Fuel " + furnace.getBurnTime() + " +" + event.getBurnTime());
                            this.startSmelting(recipe, furnace, times);
                            smeltMap.get(location).updateLastFuelTick(event.getBurnTime());
                            return;
                        }
                    }
                    invalidRecipe = true;
                }
            }
            if (invalidRecipe)
            {
                event.setCancelled(true);
            }
        }
    }

    private void startSmelting(final FurnaceRecipe recipe, final Furnace furnace, final Pair<Integer, Integer> times)
    {
        Location location = furnace.getLocation();
        CustomSmelting smelting = smeltMap.get(location);
        if (smelting == null)
        {
            Profiler.endProfiling("tester", TimeUnit.MILLISECONDS);
            Profiler.startProfiling("tester");
            System.out.print("START F:" + furnace.getBurnTime());
            smeltMap.put(location, new CustomSmelting(FurnaceManager.this, furnace, recipe, times));
        }
        else
        {
            System.out.print("CONTINUE F:" + furnace.getBurnTime() + " C: " + smelting.curSmeltTime);
            smelting.updateLastFuelTick(furnace.getBurnTime());
        }
    }

    private void fuelEnd(final Furnace furnace, final Location location)
    {
        System.out.print("Fuel End " + furnace.getBurnTime());
        this.fuelMap.remove(location);
        ItemStack fuel = furnace.getInventory().getFuel();
        CustomSmelting smelt = this.smeltMap.remove(location);
        if (fuel == null || fuel.getType() == Material.AIR)
        {
            return;
        }
        ItemStack smelting = furnace.getInventory().getSmelting();
        if (smelting == null || smelting.getType() == Material.AIR)
        {
            return;
        }
        if (smelt != null)
        {
            if (smelt.recipe.matchesRecipe(smelting))
            {
                Pair<Integer, Integer> times = smelt.recipe.getIngredients().getTimes(fuel, smelting);
                if (times != null)
                {
                    if (times.getRight() != 0)
                    {
                        this.smeltMap.put(location, smelt);
                    }
                    else
                    {
                        smelt.stop();
                    }
                }
            } // else no match -> no smelt
        }
    }

    private void restartSmelting(final FurnaceRecipe recipe, final Furnace furnace, final Location location)
    {
        if (smeltMap.get(location) != null)
        {
            return; // already smelting smth else
        }
        this.manager.core.getTaskManager().runTask(coreModule,
               new Runnable()
               {
                   @Override
                   public void run()
                   {
                       Pair<Integer, Integer> times = recipe.getIngredients()
                                                            .getTimes(fuelMap.get(location), furnace.getInventory().getSmelting());
                       if (times != null) // can smelt ?
                       {
                           if (times.getRight() != DEFAULT_SMELT_TIME)
                           {
                               System.out.print("Restart");
                               startSmelting(recipe, furnace, times);
                           }
                       } // else other recipe
                   }
               });

    }

    @EventHandler
    public void onSmelt(FurnaceSmeltEvent event)
    {
        if (event.getBlock().getState() instanceof Furnace)
        {
            final Furnace furnace = (Furnace)event.getBlock().getState();
            final Location location = furnace.getLocation();
            CustomSmelting smelt = this.smeltMap.remove(location);

            if (smelt == null)
            {
                return;
                // TODO vanilla recipe?
            }
            Pair<Integer, Integer> times = smelt.recipe.getIngredients()
                         .getTimes(this.fuelMap.get(location), event.getSource());
            if (times == null) // no match /w customSmelting
            {
                // TODO if custom fuel time need to restart cooking timer!
            }
            else // prepared custom smelting
            {
                if (times.getLeft() == 0)
                {
                    this.manager.core.getLog().warn("Someone almost smelted with an invalid fuel!");
                    event.setCancelled(true); // invalid fuel
                    return;
                }
                ItemStack result = smelt.recipe.getResult(null);
                if (result == null)
                {
                    event.setCancelled(true);
                    return;
                }
                event.setResult(result);
                System.out.print("### SMELT ###" + (float)Profiler.endProfiling("tester", TimeUnit.MILLISECONDS) / 50);
                // TODO recipe.getIngredients().getIngredientResult(event.getSource());
                if (furnace.getBurnTime() != 0)
                {
                    this.restartSmelting(smelt.recipe, furnace, location);
                }
                //event.getSource()
            }
        }
    }
}
