package de.cubeisland.cubeengine.social.interactions;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.social.Social;
import de.cubeisland.cubeengine.social.sites.facebook.FacebookUser;
import org.bukkit.entity.Player;

public class SocialCommand
{
    private final Social module;

    public SocialCommand(Social module)
    {
        this.module = module;
    }

    @Command(names = {
        "facebook", "fb"
    }, desc = "Facebook", params = {
        @Param(names = {
                                "User", "u"
        }, types = User.class)
    })
    public void facebook(CommandContext context)
    {
        if (context.getSenderAsUser() == null && !context.hasNamed("User"))
        {
            context.sendMessage("Social", "You have to include a player to log in");
        }

        User user;
        if (context.hasNamed("User"))
        {
            user = context.getNamed("User", User.class);
        }
        else
        {
            user = context.getSenderAsUser();
        }

        context.sendMessage("social", "Here is your auth address: %s", module.getFacebookManager().getAuthURL());
        context.sendMessage("social", "Please follow the link and do this command after: /facebook Code <Your auth code>");

        // @Quick_Wango This is where you need to get the response
    }
}
