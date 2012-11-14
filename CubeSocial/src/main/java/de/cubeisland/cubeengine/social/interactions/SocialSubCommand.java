package de.cubeisland.cubeengine.social.interactions;

import com.restfb.exception.FacebookException;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.social.Social;

import java.util.logging.Level;

public class SocialSubCommand
{
    private final Social module;

    public SocialSubCommand(Social module)
    {
        this.module = module;
    }

    @Command
            (
                    desc = "post a message"
            )
    public void post(CommandContext context)
    {
        if (module.getFacebookManager().hasUser(context.getSenderAsUser()))
        {
            StringBuilder message = new StringBuilder();
            for (int x = 0; x < context.indexedCount(); x++)
            {
                message.append(context.getString(x)).append(' ');
            }

            try
            {
                context.sendMessage("social", "Your message has been posted, id: %s",
                        module.getFacebookManager().getUser(context.getSenderAsUser()).publishMessage(message.toString()).getId());
            }
            catch(FacebookException ex)
            {
                context.sendMessage("social", "Your message could for some reason not be sent.");
                context.sendMessage("social", "The error message: %s", ex.getLocalizedMessage());
            }
        }
        else
        {
            context.sendMessage("shout", "You have to be a player to use this command");
        }
    }

    @Command
            (
                    desc = "sign like!",
                    params = {
                            @Param(names = {"Message", "M"}, types = String.class)
                    }
            )
    public void sign(CommandContext context)
    {
        // TODO assign a post to a sign
    }

}
