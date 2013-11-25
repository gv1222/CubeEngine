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
package de.cubeisland.engine.core.recipe.condition.logic;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.recipe.condition.ingredient.MaterialProvider;

public class OrCondition extends Condition implements MaterialProvider
{
    private Condition left;
    private Condition right;

    OrCondition(Condition left, Condition right)
    {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean check(Player player, ItemStack itemStack)
    {
        return left.check(player, itemStack) || right.check(player, itemStack);
    }

    @Override
    public Set<Material> getMaterials(Set<Material> set)
    {
        if (right instanceof MaterialProvider)
        {
            set = ((MaterialProvider)right).getMaterials(set);
        }
        if (left instanceof MaterialProvider)
        {
            set = ((MaterialProvider)left).getMaterials(set);
        }
        return set;
    }
}
