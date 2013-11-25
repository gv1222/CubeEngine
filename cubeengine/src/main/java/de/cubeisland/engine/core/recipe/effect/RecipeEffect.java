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
package de.cubeisland.engine.core.recipe.effect;

import org.bukkit.entity.Player;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.recipe.condition.Condition;

public abstract class RecipeEffect
{
    // Effects:
    // - command
    // - chat
    // - message
    // - broadcast

    // - explode
    // - setfire
    // - sound / visual
    // - summon
    // - setblock (drop existing)
    // - potionreffect
    // - firework

    // - modify exp/lvl
    // - modify money (needs Economy Service)

    /**
     * Returns true if an effect ran
     *
     * @param core
     * @param player
     * @return
     */
    public abstract boolean runEffect(Core core, Player player);

    public RecipeEffect delayedBy(long ticks)
    {
        return new DelayedEffect(ticks, this);
    }

    public RecipeEffect and(RecipeEffect effect)
    {
        return new AndEffect(this, effect);
    }

    public RecipeEffect or(RecipeEffect effect)
    {
        return new OrEffect(this, effect);
    }

    public RecipeEffect when(Condition condition)
    {
        return ConditionEffect.of(condition, this);
    }

    public RecipeEffect withChance(float chance)
    {
        return ConditionEffect.ofChance(chance, this);
    }
}
