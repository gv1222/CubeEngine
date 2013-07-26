package de.cubeisland.cubeengine.social;

import de.cubeisland.cubeengine.social.interactions.SocialCommand;
import de.cubeisland.cubeengine.social.interactions.SocialListener;
import de.cubeisland.cubeengine.social.interactions.SocialSubCommand;
import de.cubeisland.cubeengine.social.sites.facebook.FacebookManager;

import de.cubeisland.engine.core.command.CommandManager;
import de.cubeisland.engine.core.command.reflected.ReflectedCommand;
import de.cubeisland.engine.core.config.Configuration;
import de.cubeisland.engine.core.module.Module;

public class Social extends Module
{
    public FacebookManager facebookManager;
    private SocialConfig config;

    @Override
    public void onEnable()
    {
        this.config = Configuration.load(SocialConfig.class, this);
        this.facebookManager = new FacebookManager(config);

        if (!this.facebookManager.initialize())
        {
            this.getLog().error("Facebook could not be initialized. The module is shutting down.");
            this.getCore().getModuleManager().disableModule(this);
            return;
        }

        CommandManager cm = this.getCore().getCommandManager();
        cm.registerCommands(this, new SocialCommand(this), ReflectedCommand.class);
        cm.registerCommands(this, new SocialSubCommand(this), ReflectedCommand.class, "facebook");
        this.getCore().getEventManager().registerListener(this, new SocialListener(this));
    }

    public FacebookManager getFacebookManager()
    {
        return facebookManager;
    }
}
