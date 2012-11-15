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
                            "Token", "t"
                            }, types = String.class), //This token can be generated for use at: http://developers.facebook.com/tools/explorer
                            @Param(names = {
                            "User", "u"
                            }, types = User.class)
    })
    public void facebook(CommandContext context)
    {
        if (context.hasNamed("Token"))
        {
            if (context.hasNamed("User"))
            {
                if (module.getFacebookManager().hasUser(context.getNamed("User", User.class)) || module.getFacebookManager().initializeUser(context.getNamed("User", User.class), context.getString("Token")))
                {
                    FacebookUser facebook = module.getFacebookManager().getUser(context.getNamed("User", User.class));
                    context.sendMessage("social", "Your name is: %s", facebook.getUserInfo().getName());
                    context.sendMessage("social", "Your nickname on facebook is: %s", facebook.getUserInfo().getUsername());
                }
                else
                {
                    context.sendMessage("social", "You could not be initialized, maybe your token is invalid?");
                }
            }
            else if (context.getSender() instanceof Player)
            {
                if (module.getFacebookManager().hasUser(context.getSenderAsUser()) || module.getFacebookManager().initializeUser(context.getSenderAsUser(), context.getString("Token")))
                {
                    FacebookUser facebook = module.getFacebookManager().getUser(context.getSenderAsUser());
                    context.sendMessage("social", "Your name is: %s", facebook.getUserInfo().getName());
                    context.sendMessage("social", "Your nickname on facebook is: %s", facebook.getUserInfo().getUsername());
                }
                else
                {
                    context.sendMessage("social", "You could not be initialized, maybe your token is invalid?");
                }
            }
            else
            {
                context.sendMessage("social", "You have to include the User parameter when you are console");
            }

        }
        else
        {
            context.sendMessage("social", "You have to include the token parameter");
        }
    }
}
