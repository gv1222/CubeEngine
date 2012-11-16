package de.cubeisland.cubeengine.social;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.log.LogLevel;
import de.cubeisland.cubeengine.social.interactions.SocialCommand;
import de.cubeisland.cubeengine.social.interactions.SocialListener;
import de.cubeisland.cubeengine.social.interactions.SocialSubCommand;
import de.cubeisland.cubeengine.social.sites.facebook.FacebookManager;

public class Social extends Module
{
    public FacebookManager facebookManager;
    private SocialCommand baseCommand;
    private SocialSubCommand subCommand;
    private SocialConfig config;

    @Override
    public void onEnable()
    {
        this.facebookManager = new FacebookManager(config);
        this.baseCommand = new SocialCommand(this);
        this.subCommand = new SocialSubCommand(this);

        if (!this.facebookManager.initialize())
        {
            this.getLogger().log(LogLevel.ERROR, "Facebook could not be initialized. The module is shutting down.");
            this.getModuleManager().disableModule(this);
            return;
        }

        this.registerCommands(baseCommand);
        this.registerCommands(subCommand, "facebook");
        this.registerListener(new SocialListener(this));
    }

    public FacebookManager getFacebookManager()
    {
        return facebookManager;
    }
}
