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

/**
 * Modifies a resulting ItemStack
 * <p>This may be used as result for Ingredients BUT ALSO as result for the crafted item
 */
public abstract class IngredientResult
{
    public abstract ItemStack getResult(Player player, ItemStack itemStack);

    public final IngredientResult or(IngredientResult other)
    {
        return new OrResult(this, other);
    }

    public final IngredientResult and(IngredientResult other)
    {
        return new AndResult(this, other);
    }

    public final IngredientResult withChance(float chance)
    {
        return ConditionResult.ofChance(chance, this);
    }

    public final IngredientResult withCondition(Condition condition)
    {
        return ConditionResult.of(condition, this);
    }

    // cloneingredient (data/amount/enchants/name/lore/special(leatherdye/firework/book/skull...)/allmeta(ench/name/lore/special)/all(allmeta/data/amount))
    // itemname / itemlore
    // leathercolor rgb
    // bookitem title / author / pages
    // firework / firework charge item
    //  color rgb,...
    //  fadecolor rgb,...
    //  type ball ball_large star burst creeper
    //  trail / flicker
    // power 0-128
    // skullowner
    // potionitem type/lv/extended/splash  + moar custom effects (type,duration,amplify,ambient?)
    // enchantitem / book

}
