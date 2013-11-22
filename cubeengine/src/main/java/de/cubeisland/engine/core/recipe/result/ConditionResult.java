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
package de.cubeisland.engine.core.recipe.result;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.recipe.condition.Condition;
import de.cubeisland.engine.core.recipe.condition.general.ChanceCondition;

public class ConditionResult extends IngredientResult
{
    private Condition condition;
    private IngredientResult result;

    private ConditionResult(Condition condition, IngredientResult result)
    {
        this.condition = condition;
        this.result = result;
    }

    @Override
    public ItemStack getResult(Player player, ItemStack itemStack)
    {
        if (condition.check(player, itemStack))
        {
            return result.getResult(player, itemStack);
        }
        return null;
    }

    public static ConditionResult of(Condition condition, IngredientResult result)
    {
        return new ConditionResult(condition, result);
    }

    public static ConditionResult ofChance(float chance, IngredientResult result)
    {
        return new ConditionResult(ChanceCondition.of(chance), result);
    }
}
