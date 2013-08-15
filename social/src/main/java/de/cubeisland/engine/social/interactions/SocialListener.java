/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.social.interactions;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import de.cubeisland.engine.social.Social;

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
