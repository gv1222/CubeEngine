package de.cubeisland.cubeengine.social;

import com.restfb.FacebookClient;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.social.interactions.SocialCommand;
import de.cubeisland.cubeengine.social.interactions.SocialListener;
import de.cubeisland.cubeengine.social.interactions.SocialSubCommand;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class Social extends Module
{
    SocialCommand baseCommand;
    SocialSubCommand subCommand;
    public final Map<String, FacebookClient> clients = new HashMap<String, FacebookClient>();
    public final HashMap<Location, String> posts = new HashMap<Location, String>();

    @Override
    public void onEnable()
    {
        this.baseCommand = new SocialCommand(this);
        this.subCommand = new SocialSubCommand(this);

        this.registerCommands(baseCommand);
        this.registerCommands(subCommand, "facebook");
        this.registerListener(new SocialListener(this));
    }

}
