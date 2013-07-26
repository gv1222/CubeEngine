package de.cubeisland.engine.social.interactions;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import de.cubeisland.engine.social.Social;

import com.restfb.exception.FacebookException;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;

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
        CommandSender sender = context.getSender();
        if (sender instanceof User)
        {
            User user = (User)sender;
            StringBuilder message = new StringBuilder();
            for (int x = 0; x < context.getArgCount(); x++)
            {
                message.append(context.getString(x)).append(' ');
            }

            try
            {
                context.sendTranslated("Your message has been posted, id: %s", module.getFacebookManager().getUser(user)
                                                                                     .publishMessage(message.toString())
                                                                                     .getId());
            }
            catch (FacebookException ex)
            {
                context.sendTranslated("Your message could for some reason not be sent.");
                context.sendTranslated("The error message: %s", ex.getLocalizedMessage());
            }
        }
        else
        {
            context.sendTranslated("You have to be a player to use this command");
        }
    }

    @Command(desc = "sign like!")
    public void sign(CommandContext context)
    {
        if (!context.isSender(User.class))
        {
            context.sendTranslated("You cant execute this command from the console");
            return;
        }

        User sender = (User)context.getSender();

        Block targetBlock = sender.getTargetBlock(null, 9);
        if (targetBlock == null)
        {
            context.sendTranslated("You have to look at a sign less than 9 meters away.");
            return;
        }
        if (!(targetBlock.getType() == Material.SIGN || targetBlock.getType() == Material.WALL_SIGN || targetBlock.getType() == Material.SIGN_POST))
        {
            context.sendTranslated("You have to look at a sign less than 9 meters away");
            return;
        }

        Sign targetSign = (Sign)targetBlock.getState();
        try
        {
            StringBuilder message = new StringBuilder();
            for (int x = 0; x < context.getArgCount(); x++)
            {
                message.append(context.getString(x)).append(' ');
            }

            String id = module.getFacebookManager().getUser(sender).publishMessage(message.toString()).getId();
            module.getFacebookManager().addPosts(targetBlock.getLocation(), id);
            targetSign.setLine(0, ChatFormat.parseFormats("&b" + ChatFormat.stripFormats(targetSign.getLine(0))));
        }
        catch (Exception ex)
        {
            targetSign.setLine(0, ChatFormat.parseFormats("&c" + ChatFormat.stripFormats(targetSign.getLine(0))));
            context.sendTranslated("An error occurred while posting the message =(");
            context.sendTranslated("The error message: %s", ex.getLocalizedMessage());
        }
        finally
        {
            targetSign.update();
        }

    }

}
