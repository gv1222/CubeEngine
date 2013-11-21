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
package de.cubeisland.engine.core.recipe.ingredient.condition;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;

public class OrCondition extends IngredientCondition implements MaterialProvider
{
    private IngredientCondition left;
    private IngredientCondition right;

    public OrCondition(IngredientCondition left, IngredientCondition right)
    {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean check(Permissible permissible, ItemStack itemStack)
    {
        return left.check(permissible, itemStack) || right.check(permissible, itemStack);
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
