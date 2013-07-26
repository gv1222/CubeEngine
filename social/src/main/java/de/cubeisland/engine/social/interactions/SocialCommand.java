package de.cubeisland.engine.social.interactions;

import de.cubeisland.engine.social.Social;

import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;

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
    public void facebook(ParameterizedContext context)
    {
        if (!context.isSender(User.class) && !context.hasParam("User"))
        {
            context.sendTranslated("You have to include a player to log in");
            return;
        }

        User user;
        if (context.hasParam("User"))
        {
            user = context.getParam("User");
        }
        else
        {
            user = (User)context.getSender();
        }

        if (context.hasParam("Code"))
        {
            String verifyCode = context.getString("Code");
            module.getFacebookManager().initializeUser(user, verifyCode);
            return;
        }

        context.sendTranslated("Here is your auth address: %s", module.getFacebookManager().getAuthURL(user));

        // @Quick_Wango This is where you need to get the response
    }
}
