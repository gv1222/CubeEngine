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

import org.bukkit.Location;
import org.bukkit.block.Furnace;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.module.CoreModule;
import de.cubeisland.engine.core.util.Pair;

public class FurnaceManager implements Listener
{
    protected final CoreModule coreModule;
    protected RecipeManager manager;

    protected Map<Location, FuelIngredient> fuelMap = new HashMap<>();
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
                    Pair<FuelIngredient, Boolean> fuel = ingredients.matchFuelIngredient(event.getFuel(),
                         furnace.getInventory().getSmelting());
                    if (fuel != null && fuel.getRight())
                    {
                        event.setBurnTime(fuel.getLeft().fuelTicks);
                        fuelMap.put(location, fuel.getLeft());
                        System.out.print("New Fuel | BurnTime +" + event.getBurnTime());
                        this.startSmelting(recipe, furnace, fuel.getLeft(), event.getBurnTime());
                        return;
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

    private void startSmelting(final FurnaceRecipe recipe, final Furnace furnace, final FuelIngredient times, int burnTime)
    {
        Location location = furnace.getLocation();
        CustomSmelting smelting = smeltMap.get(location);
        if (smelting == null)
        {
            System.out.print("Start Smelt | Fuel " + burnTime);
            smeltMap.put(location, new CustomSmelting(FurnaceManager.this, furnace, recipe, times)
                .updateLastFuelTick(burnTime));
        }
        else
        {
            System.out.print("Continue Smelt | Fuel " + burnTime +
                                 " C:" + smelting.curSmeltTime + "/" + smelting.totalSmeltTime);
            smelting.updateLastFuelTick(burnTime + smelting.getLastFuelTick());
        }
    }

    private void restartSmelting(final FurnaceRecipe recipe, final Furnace furnace, final Location location, final int endFuel)
    {
        if (endFuel == 0)
        {
            return;
        }
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
                       if (recipe.ingredients.isSmeltable(furnace.getInventory().getSmelting()))
                       {
                           System.out.print("Restart");
                           startSmelting(recipe, furnace, fuelMap.get(location), endFuel);
                       }
                       else
                       {
                            System.out.print("No Restart!");
                            // TODO search recipe OR do not continue smelt animation
                       }
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
                if (fuelMap.get(location) != null)
                {
                    System.out.print("Custom Fuel Detected!");
                    event.setCancelled(true);
                }
                // TODO vanilla recipe?
                return;
            }
            ItemStack result = smelt.recipe.getResult(null, furnace);
            if (result == null)
            {
                event.setCancelled(true);
                return;
            }
            event.setResult(result);
            System.out.print("### SMELT ###");
            final ItemStack ingredientResult = smelt.recipe.getIngredients().getIngredientResult(event.getSource().clone(), furnace);
            if (ingredientResult != null)
            {
                this.manager.core.getTaskManager().runTask(coreModule,
                   new Runnable()
                   {
                       @Override
                       public void run()
                       {
                           furnace.getInventory().setSmelting(ingredientResult);
                           for (HumanEntity humanEntity : furnace.getInventory().getViewers())
                           {
                               if (humanEntity instanceof Player)
                               {
                                   ((Player)humanEntity).updateInventory();
                               }
                           }
                       }
                   });
            }

            if (furnace.getBurnTime() > 0)
            {
                this.restartSmelting(smelt.recipe, furnace, location, smelt.getEndFuel());
            }
            else
            {
                this.fuelMap.remove(location);
            }
        }
    }

    @EventHandler
    public void onNewSmeltable(InventoryClickEvent event)
    {
        // TODO prevent taking out when fuel not used up / add preview for furnacerecipe
        // TODO FurnaceExtractEvent
        if (event.getInventory() instanceof FurnaceInventory)
        {
            if (event.getInventory().getHolder() instanceof Furnace)
            {
                Furnace furnace = (Furnace)event.getInventory().getHolder();
                if (furnace.getBurnTime() <= 0)
                {
                    return;
                }
                //ItemStack oldItem = furnace.getInventory().getSmelting();
                ItemStack newItem = null;
                switch (event.getAction())
                {
                case SWAP_WITH_CURSOR:
                case PLACE_ALL:
                    if (event.getRawSlot() == 0)
                    {
                        newItem = event.getCursor();
                        break;
                    }
                    return;
                case HOTBAR_SWAP:
                    if (event.getRawSlot() == 0)
                    {
                        newItem = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
                        break;
                    }
                    return;
                case PLACE_ONE:
                    if (event.getRawSlot() != 0 || event.getCursor().isSimilar(furnace.getInventory().getSmelting()))
                    {
                        return;
                    }
                    newItem = event.getCursor();
                    break;
                case PICKUP_ALL:
                    if (event.getRawSlot() == 0)
                    {
                        break;
                    }
                    return;
                case MOVE_TO_OTHER_INVENTORY:
                    if (event.getRawSlot() == 0)
                    {
                        break;
                    }
                    else if (event.getRawSlot() > event.getView().getTopInventory().getSize())
                    {
                        newItem = event.getCurrentItem();
                    }
                    else if (event.getSlotType() == SlotType.RESULT)
                    {
                        return; // shift click out result
                    }
                    break;
                default: return;
                }
                Location location = furnace.getLocation();
                FuelIngredient customFuel = this.fuelMap.get(location);
                CustomSmelting customSmelting = this.smeltMap.get(location);
                if (customSmelting != null)
                {
                    // TODO block changing
                    event.setCancelled(true);
                    return;
                }
                if (newItem == null)
                {
                    return;
                }
                boolean invalidRecipe = false;
                for (final FurnaceRecipe recipe : this.manager.furnaceRecipes)
                {
                    FurnaceIngredients ingredients = recipe.getIngredients();
                    if (recipe.matchesRecipe(newItem))
                    {
                        if (ingredients.hasFuel(customFuel))
                        {
                            if (ingredients.isSmeltable(newItem))
                            {
                                System.out.print("New Smeltable");
                                this.startSmelting(recipe, furnace, customFuel, furnace.getBurnTime());
                                return;
                            }
                        }
                        invalidRecipe = true;
                    }
                }
                if (invalidRecipe || customFuel != null)
                {
                    event.setCancelled(true);
                    System.out.print("No valid Recipe");
                    // TODO prevent burning instead ??
                }
            }
        }
    }

    // TODO intercept putting new items in furnace when smelting a custom recipe

    // exp gained etc
}
