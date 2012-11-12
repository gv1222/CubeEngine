package de.cubeisland.cubeengine.social;

import com.restfb.Parameter;
import com.restfb.types.FacebookType;
import com.restfb.types.User;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Param;

public class SocialSubCommand
{
    SocialCommand parent;

    public SocialSubCommand(SocialCommand sc)
    {
        this.parent = sc;
    }

    @Command
            (
                    desc = "post a message",
                    params = {
                            @Param(names = {"Post", "p"}, types = String.class)
                    }
            )
    public void post(CommandContext context)
    {
        if (context.hasNamed("Post") && parent.clients.containsKey(context.getSender().getName()))
        {
            FacebookType publishMessageResponse = parent.clients.get(context.getSender().getName()).publish("me/feed", FacebookType.class, Parameter.with("message", context.getString("post")));
            context.sendMessage("shout", "Your message id was: %s", publishMessageResponse.getId());
        }
        else
        {
            context.sendMessage("shout", "You have to do post, or you are not initialized");
        }
    }

}
