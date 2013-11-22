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
import de.cubeisland.engine.core.recipe.condition.general.ChanceCondition;

public class ConditionEffect extends RecipeEffect
{
    private Condition condition;
    private RecipeEffect effect;

    private ConditionEffect(Condition condition, RecipeEffect effect)
    {
        this.condition = condition;
        this.effect = effect;
    }

    public static ConditionEffect of(Condition condition, RecipeEffect effect)
    {
        return new ConditionEffect(condition, effect);
    }

    public static ConditionEffect ofChance(float chance, RecipeEffect effect)
    {
        return new ConditionEffect(ChanceCondition.of(chance), effect);
    }

    @Override
    public void runEffect(Core core, Player player)
    {

    }
}
