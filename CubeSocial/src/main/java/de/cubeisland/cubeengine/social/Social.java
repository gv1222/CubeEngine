package de.cubeisland.cubeengine.social;

import de.cubeisland.cubeengine.core.module.Module;

public class Social extends Module
{
    SocialCommand baseCommand;

    @Override
    public void onEnable()
    {
        this.baseCommand = new SocialCommand();
        this.registerCommands(baseCommand);
        this.registerCommands(new SocialSubCommand(baseCommand), "facebook");
    }

}
