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

import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;

/**
 * Actually checks the durability (which is the materialData somehow)
 */
public class DataCondition extends IngredientCondition
{
    private enum Type
    {
        EXACT, MORE, LESS, BIT;
    }

    private Type type;
    private byte data;

    private DataCondition(Type type, byte data)
    {
        this.type = type;
        this.data = data;
    }

    public static DataCondition less(byte data)
    {
        return new DataCondition(Type.LESS, data);
    }

    public static DataCondition more(byte data)
    {
        return new DataCondition(Type.MORE, data);
    }

    public static DataCondition exact(byte data)
    {
        return new DataCondition(Type.EXACT, data);
    }

    public static IngredientCondition notRange(byte from, byte to)
    {
        return DataCondition.range(from, to).not();
    }

    public static IngredientCondition range(byte from, byte to)
    {
        return DataCondition.more(from).and(DataCondition.less(to));
    }

    public static DataCondition bitSet(byte bit)
    {
        return new DataCondition(Type.BIT, bit);
    }

    @Override
    protected boolean check(Permissible permissible, ItemStack itemStack)
    {
        switch (this.type)
        {
            case EXACT:
                return itemStack.getDurability() == data;
            case MORE:
                return itemStack.getDurability() > data;
            case LESS:
                return itemStack.getDurability() < data;
            case BIT:
                return (itemStack.getDurability() & data) == data;
        }
        throw new IllegalStateException();
    }
}
