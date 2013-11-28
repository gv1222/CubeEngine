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

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Furnace;

import de.cubeisland.engine.core.module.CoreModule;

import static de.cubeisland.engine.core.recipe.FuelIngredient.DEFAULT_SMELT_TIME;

public class CustomSmelting
{
    public final FurnaceRecipe recipe;
    private final Location location;
    public final int totalSmeltTime;
    protected int curSmeltTime;
    private final float factor;
    private final CoreModule coreModule;

    public int getLastFuelTick()
    {
        return lastFuelTick;
    }

    private int lastFuelTick = -1;

    public int n;
    public static int N;

    private int endFuel;

    public CustomSmelting updateLastFuelTick(int ticks)
    {
        lastFuelTick = ticks;
        return this;
    }

    public CustomSmelting(final FurnaceManager manager, final Furnace furnace, FurnaceRecipe setRecipe, final FuelIngredient fuel)
    {
        n = N++;
        this.recipe = setRecipe;
        this.location = furnace.getLocation();
        this.totalSmeltTime = fuel.smeltTicks;
        this.factor = (float)totalSmeltTime / DEFAULT_SMELT_TIME;
        this.coreModule = manager.coreModule;
        this.curSmeltTime = 0;
        Runnable runner = new Runnable()
        {
            private boolean noFuel = false;

            @Override
            public void run()
            {
                // TODO stop when result is full  OR DropResult drops the item to the ground
                if (noFuel || furnace.getInventory().getSmelting() == null || furnace.getInventory().getSmelting().getType() == Material.AIR)
                {
                    manager.smeltMap.remove(location);
                    System.out.print("Abort Smelt: nothing to smelt OR no more fuel");
                    return;
                }
                if (furnace.getBurnTime() <= 0 && lastFuelTick <= 0)
                {
                    this.noFuel = true;
                }
                if (manager.fuelMap.get(location) == fuel)
                {
                    if (!recipe.getIngredients().isSmeltable(furnace.getInventory().getSmelting()))
                    {
                        System.out.print("Abort Smelt: Smelt changed");
                        return;
                    }
                }
                else
                {
                    // TODO handle fuel change
                    System.out.print("Abort Smelt: Fuel changed");
                    return;
                }
                curSmeltTime += lastFuelTick - furnace.getBurnTime();
                short cookTime = (short)(curSmeltTime / factor);
                //System.out.print("#" + n + " | " + curSmeltTime + "/" + totalSmeltTime  +
                //                     " | RealFuel:" + furnace.getBurnTime());
                updateLastFuelTick(furnace.getBurnTime());
                if (curSmeltTime < totalSmeltTime)
                {
                    if (totalSmeltTime > DEFAULT_SMELT_TIME)
                    {
                        if (cookTime >= DEFAULT_SMELT_TIME - 5)
                        {
                            cookTime -= 5; // prevent smelting too soon
                        }
                    }
                    furnace.setCookTime(cookTime);
                    coreModule.getCore().getTaskManager().runTaskDelayed(coreModule, this, 1);
                }
                else
                {
                    endFuel = (short)(furnace.getBurnTime() + curSmeltTime - totalSmeltTime);
                    if (furnace.getBurnTime() <= 0)
                    {
                        System.out.print("Last Smelt! (Fuel +1)");
                        furnace.setBurnTime((short)1);
                    }
                    furnace.setCookTime((short)(DEFAULT_SMELT_TIME));
                    System.out.print("Smelt Done | EndFuel " + endFuel +"(+" +(curSmeltTime - totalSmeltTime) + ")");
                }
            }
        };
        manager.coreModule.getCore().getTaskManager().runTaskDelayed(manager.coreModule, runner, 1);
    }

    public int getEndFuel()
    {
        return endFuel;
    }
}
