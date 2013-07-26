package de.cubeisland.cubeengine.social.interactions;

import com.restfb.exception.FacebookException;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.social.Social;
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

    @Command(desc = "post a message")
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
            catch (FacebookException ex)
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

    @Command(desc = "sign like!")
    public void sign(CommandContext context)
    {
        if (context.getSenderAsUser() == null)
        {
            context.sendMessage("social", "You cant execute this command from the console");
            return;
        }

        Block targetBlock = context.getSenderAsUser().getTargetBlock(null, 9);
        if (targetBlock == null)
        {
            context.sendMessage("social", "You have to look at a sign less than 9 meters away.");
            return;
        }
        if (!(targetBlock.getType() == Material.SIGN || targetBlock.getType() == Material.WALL_SIGN || targetBlock.getType() == Material.SIGN_POST))
        {
            context.sendMessage("social", "You have to look at a sign less than 9 meters away");
            return;
        }

        Sign targetSign = (Sign)targetBlock.getState();
        try
        {
            StringBuilder message = new StringBuilder();
            for (int x = 0; x < context.indexedCount(); x++)
            {
                message.append(context.getString(x)).append(' ');
            }

            String id = module.getFacebookManager().getUser(context.getSenderAsUser()).publishMessage(message.toString()).getId();
            module.getFacebookManager().addPosts(targetBlock.getLocation(), id);
            targetSign.setLine(0, ChatFormat.parseFormats("&b" + ChatFormat.stripFormats(targetSign.getLine(0))));
        }
        catch (Exception ex)
        {
            targetSign.setLine(0, ChatFormat.parseFormats("&c" + ChatFormat.stripFormats(targetSign.getLine(0))));
            context.sendMessage("social", "An error occurred while posting the message =(");
            context.sendMessage("social", "The error message: %s", ex.getLocalizedMessage());
        }
        finally
        {
            targetSign.update();
        }

    }

}
