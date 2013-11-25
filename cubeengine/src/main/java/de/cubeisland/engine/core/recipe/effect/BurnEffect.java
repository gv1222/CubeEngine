package de.cubeisland.engine.core.recipe.effect;

import org.bukkit.entity.Player;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.recipe.effect.logic.Effect;

public class BurnEffect extends Effect
{
    private int fireTicks;

    public BurnEffect(int fireTicks)
    {
        this.fireTicks = fireTicks;
    }

    @Override
    public boolean runEffect(Core core, Player player)
    {
        player.setFireTicks(fireTicks);
        return true;
    }
}
