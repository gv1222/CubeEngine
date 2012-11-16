package de.cubeisland.cubeengine.social.interactions;

import com.restfb.exception.FacebookException;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.social.Social;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;

public class SocialListener implements Listener
{
    private final Social module;
    private final UserManager userManager;

    public SocialListener(Social module)
    {
        this.module = module;
        this.userManager = module.getUserManager();
    }

    @EventHandler
    public void signInteract(PlayerInteractEvent event)
    {
        if (module.getFacebookManager().hasPost(event.getClickedBlock().getLocation()))
        {
            User user = userManager.getExactUser(event.getPlayer());
            if (!module.getFacebookManager().hasUser(user))
            {
                user.sendMessage("Social", "You are not logged into facebook");
                return;
            }
            String postId = module.getFacebookManager().fetchPost(event.getClickedBlock().getLocation());
            try
            {
                module.getFacebookManager().getUser(user).likeObject(postId);
                user.sendMessage("social", "You have liked the post");

            }
            catch (FacebookException ex)
            {
                user.sendMessage("social", "The post could not be liked.");
                user.sendMessage("social", "The error was: %s", ex.getLocalizedMessage());
            }
        }
        module.getLogger().info("fail");

    }

}
