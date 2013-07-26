package de.cubeisland.cubeengine.social.interactions;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import de.cubeisland.cubeengine.social.Social;

import com.restfb.exception.FacebookException;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserManager;

public class SocialListener implements Listener
{
    private final Social module;
    private final UserManager userManager;

    public SocialListener(Social module)
    {
        this.module = module;
        this.userManager = module.getCore().getUserManager();
    }

    @EventHandler
    public void signInteract(PlayerInteractEvent event)
    {
        if (module.getFacebookManager().hasPost(event.getClickedBlock().getLocation()))
        {
            User user = userManager.getExactUser(event.getPlayer().getName());
            if (!module.getFacebookManager().hasUser(user))
            {
                user.sendTranslated("You are not logged into facebook");
                return;
            }
            String postId = module.getFacebookManager().fetchPost(event.getClickedBlock().getLocation());
            try
            {
                module.getFacebookManager().getUser(user).likeObject(postId);
                user.sendTranslated("You have liked the post");

            }
            catch (FacebookException ex)
            {
                user.sendTranslated("The post could not be liked.");
                user.sendTranslated("The error was: %s", ex.getLocalizedMessage());
            }
        }
        module.getLog().info("fail");

    }

}
