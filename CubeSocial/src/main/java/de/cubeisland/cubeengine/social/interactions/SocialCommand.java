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
        }, type = User.class),
            @Param(names = {
                "Code", "c"
            }, type = String.class)
    })
    public void facebook(CommandContext context)
    {
        if (context.getSenderAsUser() == null && !context.hasNamed("User"))
        {
            context.sendMessage("Social", "You have to include a player to log in");
            return;
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

        if (context.hasNamed("Code"))
        {
            String verifyCode = context.getString("Code");
            module.getFacebookManager().initializeUser(user, verifyCode);
            return;
        }

        context.sendMessage("social", "Here is your auth address: %s", module.getFacebookManager().getAuthURL());

        // @Quick_Wango This is where you need to get the response
    }
}
