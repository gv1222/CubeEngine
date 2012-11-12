package de.cubeisland.cubeengine.social;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class SocialCommand
{
    public final Map<String, FacebookClient> clients;

    public SocialCommand()
    {
        clients = new HashMap<String, FacebookClient>();
        clients.put("public", new com.restfb.DefaultFacebookClient());
    }

    @Command
    (
            names = {"facebook", "fb"},
            desc = "Facebook",
            params = {
                    @Param(names= {"Token", "t"}, types = String.class)
            }
    )
    public void facebook(CommandContext context)
    {
        if (clients.containsKey(context.getSender()))
        {
            context.sendMessage("You are initialized");
            FacebookClient client = clients.get(context);
            com.restfb.types.User fUser = client.fetchObject("me", com.restfb.types.User.class);
            context.sendMessage("social", "Your name is: {0}", fUser.getName());
            context.sendMessage("social", "Your facebook username is: {0}", fUser.getUsername());
        }
        else if (context.getString("Token") != null)
        {
            clients.put(context.getSender().getName(), new com.restfb.DefaultFacebookClient(context.getString("Token")));
            FacebookClient client = clients.get(context.getSender().getName());
            com.restfb.types.User fUser = client.fetchObject("me", com.restfb.types.User.class);
            context.sendMessage("social", "Your name is: %s", fUser.getName());
            context.sendMessage("social", "Your facebook username is: %s", fUser.getUsername());
        }
    }
}
