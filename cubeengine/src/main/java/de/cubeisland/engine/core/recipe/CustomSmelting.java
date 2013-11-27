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

import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Furnace;

import de.cubeisland.engine.core.module.CoreModule;
import de.cubeisland.engine.core.util.Pair;
import de.cubeisland.engine.core.util.Profiler;

import static de.cubeisland.engine.core.recipe.FuelIngredient.DEFAULT_SMELT_TIME;

public class CustomSmelting
{
    public final FurnaceRecipe recipe;
    private final Location location;
    public final int totalSmeltTime;
    protected int curSmeltTime;
    private final float factor;
    private final CoreModule coreModule;

    private int lastFuelTick;
    private boolean stop = false;

    public void updateLastFuelTick(int ticks)
    {
        lastFuelTick = ticks;
    }

    public void stop()
    {
        this.stop = true;
    }

    public CustomSmelting(final FurnaceManager manager, final Furnace furnace, FurnaceRecipe setRecipe, Pair<Integer, Integer> times)
    {
        this.recipe = setRecipe;
        this.location = furnace.getLocation();
        this.totalSmeltTime = times.getRight();
        this.factor = (float)totalSmeltTime / DEFAULT_SMELT_TIME;
        this.coreModule = manager.coreModule;
        this.curSmeltTime = 0;
        this.updateLastFuelTick(furnace.getBurnTime());

        Runnable runner = new Runnable()
        {
            @Override
            public void run()
            {
                if (stop)
                {
                    return;
                }
                if (lastFuelTick == 0)
                {
                    return;
                }
                if (furnace.getInventory().getSmelting() == null || furnace.getInventory().getSmelting().getType() == Material.AIR)
                {
                    return;
                }
                if (recipe.getIngredients().getTimes(manager.fuelMap.get(location),
                                                     furnace.getInventory().getSmelting()) == null)
                {

                    // TODO handle changed item / search new Recipe
                }
                else // item has not changed
                {
                    curSmeltTime += lastFuelTick - furnace.getBurnTime();
                    short cookTime = (short)(curSmeltTime / factor);
                    System.out.print("D: " + cookTime + " A: " + curSmeltTime + "/" + totalSmeltTime  +
                                         " F:" + furnace.getBurnTime() + " LF:" + lastFuelTick +
                                         " ticks: " + (float)Profiler.getCurrentDelta("tester", TimeUnit.MILLISECONDS)/50);
                    updateLastFuelTick(furnace.getBurnTime());
                    if (curSmeltTime < totalSmeltTime - 1)
                    {
                        if (cookTime >= DEFAULT_SMELT_TIME - 1)
                        {
                            cookTime = DEFAULT_SMELT_TIME - 2;
                        }
                        furnace.setCookTime(cookTime);
                        coreModule.getCore().getTaskManager().runTaskDelayed(coreModule, this, 1);
                    }
                    else
                    {
                        furnace.setCookTime((short)(DEFAULT_SMELT_TIME));
                        System.out.print("Smelt SOON!");
                    }
                }
            }
        };
        manager.coreModule.getCore().getTaskManager().runTaskDelayed(manager.coreModule, runner, 1);
    }
}
