package de.cubeisland.cubeengine.social;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.FacebookType;
import com.restfb.types.Page;
import com.restfb.types.User;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class SocialSubCommand
{
    private final Social module;

    public SocialSubCommand(Social module)
    {
        this.module = module;
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
        if (context.hasNamed("Post") && module.clients.containsKey(context.getSender().getName()))
        {
            FacebookType publishMessageResponse = module.clients.get(context.getSender().getName()).publish("me/feed", FacebookType.class, Parameter.with("message", context.getString("post")));
            context.sendMessage("shout", "Your message id was: %s", publishMessageResponse.getId());
        }
        else
        {
            context.sendMessage("shout", "You have to do post, or you are not initialized");
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
    { // This don't work
        Block targetBlock = context.getSenderAsUser("shout", "").getTargetBlock(null, 9);
        if (context.hasNamed("Message") && targetBlock != null && (targetBlock.getType() == Material.SIGN || targetBlock.getType() == Material.WALL_SIGN) && module.clients.containsKey(context.getSender().getName()))
        {
            Sign sign = (Sign)targetBlock.getState();
            if (sign.getLine(0).equalsIgnoreCase("[FaceBook]"))
            {
                FacebookClient client = module.clients.get(context.getSender().getName());
                Page page = client.fetchObject(sign.getLine(1), Page.class);
                if(page.getLikes() != null)//this will be false if the page isn't a page
                {
                    context.sendMessage("Your page id is: " + page.getId());
                    FacebookClient pageClient = new DefaultFacebookClient(page.getAccessToken());
                    FacebookType publishMessageResponse = pageClient.publish(page.getId()+"/feed", FacebookType.class, Parameter.with("message", context.getString("Message")));
                    context.sendMessage("Your message id is: " + publishMessageResponse.getId());
                    module.posts.put(targetBlock.getLocation(), publishMessageResponse.getId());
                    sign.setLine(0, "&3[Facebook]");
                    sign.update();
                }

            }
        }
    }

}
