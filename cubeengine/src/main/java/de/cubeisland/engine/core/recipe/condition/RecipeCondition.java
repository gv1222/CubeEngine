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
package de.cubeisland.engine.core.recipe.condition;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.PrepareItemCraftEvent;

import de.cubeisland.engine.core.recipe.Recipe;

public abstract class RecipeCondition
{
    private final String conditionPermission;
    private final boolean permIgnoreCondition;

    protected RecipeCondition(String perm, boolean permIgnores)
    {
        this.conditionPermission = perm;
        this.permIgnoreCondition = permIgnores;
    }

    protected RecipeCondition()
    {
        this(null, false);
    }

    /**
     * Returns true if the condition is met
     *
     * @param event
     * @return
     */
    public final boolean check(PrepareItemCraftEvent event, Recipe recipe)
    {
        if (needsCheck(event))
        {
            return process(event, recipe);
        } // else condition is ignored
        return true;
    }

    /**
     * Returns true if the condition is met
     *
     * @param event
     * @return
     */
    protected abstract boolean process(PrepareItemCraftEvent event, Recipe recipe);

    private boolean needsCheck(PrepareItemCraftEvent event)
    {
        if (event.getViewers().size() == 1)
        {
            if (conditionPermission != null)
            {
                HumanEntity humanEntity = event.getViewers().get(0);
                if (humanEntity.hasPermission(this.conditionPermission))
                {
                    return !permIgnoreCondition;
                }
                else
                {
                    return permIgnoreCondition;
                }
            }
            return true;
        }
        throw new IllegalStateException(); // TODO handle this better
    }
}

// general Conditions
// - generated Permission
// - other registered Permissions (linking /w && & ||)
// - world
// - terrain-height
// - biome
// - lightlevel
// - weather
// - exp/level
// - money (needs Economy Service)
// - itemInHand
// - gamemode
// - cooldown
// - powered by redstone

// permcheck if condition is req
