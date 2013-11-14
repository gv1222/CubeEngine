package de.cubeisland.engine.core.recipe.ingredient.condition;

import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;

public class AmountCondition extends IngredientCondition
{
    private enum Type
    {
        EXACT, MORE, LESS;
    }

    private Type type;
    private int amount;

    private AmountCondition(Type type, int amount)
    {
        this.type = type;
        this.amount = amount;
    }

    public static AmountCondition less(int data)
    {
        // TODO handle impossible
        return new AmountCondition(Type.LESS, data);
    }

    public static AmountCondition more(int data)
    {
        // TODO handle impossible
        return new AmountCondition(Type.MORE, data);
    }

    public static AmountCondition exact(int data)
    {
        return new AmountCondition(Type.EXACT, data);
    }

    public static IngredientCondition notRange(int from, int to)
    {
        return AmountCondition.range(from, to).not();
    }

    public static IngredientCondition range(int from, int to)
    {
        return AmountCondition.more(from).and(AmountCondition.less(to));
    }

    @Override
    protected boolean check(Permissible permissible, ItemStack itemStack)
    {
        switch (this.type)
        {
        case EXACT:
            return itemStack.getDurability() == amount;
        case MORE:
            return itemStack.getDurability() > amount;
        case LESS:
            return itemStack.getDurability() < amount;
        }
        throw new IllegalStateException();
    }
}
