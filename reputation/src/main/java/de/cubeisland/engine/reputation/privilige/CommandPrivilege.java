package de.cubeisland.engine.reputation.privilige;

import de.cubeisland.engine.core.user.User;

public class CommandPrivilege implements Privilege
{
    private String commandGive;
    private String commandRemove;

    @Override
    public void give(User user)
    {
        if (this.commandGive != null)
        {
            String cmd = commandGive.replace("{USER}", user.getName());
            user.getCore().getCommandManager().runCommand(user.getCore().getCommandManager().getConsoleSender(), cmd);
        }
    }

    @Override
    public void remove(User user)
    {
        if (this.commandRemove != null)
        {
            String cmd = commandRemove.replace("{USER}", user.getName());
            user.getCore().getCommandManager().runCommand(user.getCore().getCommandManager().getConsoleSender(), cmd);
        }
    }
}
