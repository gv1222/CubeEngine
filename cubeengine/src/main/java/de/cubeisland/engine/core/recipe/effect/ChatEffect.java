package de.cubeisland.engine.core.recipe.effect;

import org.bukkit.entity.Player;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.recipe.effect.logic.Effect;

public class ChatEffect extends Effect
{
    private String msg;

    public ChatEffect(String msg)
    {
        this.msg = msg;
    }

    @Override
    public boolean runEffect(Core core, Player player)
    {
        player.chat(this.msg);
        return true;
    }
}
