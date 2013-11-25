package de.cubeisland.engine.core.recipe.effect;

import org.bukkit.entity.Player;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.recipe.effect.logic.Effect;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserManager;

public class MessageEffect extends Effect
{
    private String msg;
    private Object[] args;

    private UserManager manager;

    public MessageEffect(UserManager manager, String msg, Object[]... args)
    {
        this.manager = manager;
        this.msg = msg;
        this.args = args;
    }

    @Override
    public boolean runEffect(Core core, Player player)
    {
        User user = manager.getExactUser(player.getName());
        user.sendTranslated(msg, args);
        return true;
    }
}
