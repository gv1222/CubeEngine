package de.cubeisland.cubeengine.social;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.social.interactions.SocialCommand;
import de.cubeisland.cubeengine.social.interactions.SocialListener;
import de.cubeisland.cubeengine.social.interactions.SocialSubCommand;
import de.cubeisland.cubeengine.social.sites.facebook.FacebookManager;

public class Social extends Module
{
    public FacebookManager facebookManager;
    private SocialCommand baseCommand;
    private SocialSubCommand subCommand;

    @Override
    public void onEnable()
    {
        this.facebookManager = new FacebookManager();
        this.baseCommand = new SocialCommand(this);
        this.subCommand = new SocialSubCommand(this);

        this.registerCommands(baseCommand);
        this.registerCommands(subCommand, "facebook");
        this.registerListener(new SocialListener(this));
    }

    public FacebookManager getFacebookManager()
    {
        return facebookManager;
    }
}
