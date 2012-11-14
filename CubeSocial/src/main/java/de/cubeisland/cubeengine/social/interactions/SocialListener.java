package de.cubeisland.cubeengine.social.interactions;

import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.Like;
import com.restfb.types.Page;
import de.cubeisland.cubeengine.social.Social;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;

public class SocialListener implements Listener
{
    private final Social module;
    private HashMap<Location, String> pages;

    public SocialListener(Social module)
    {
        this.module = module;
        this.pages = new HashMap<Location, String>();
    }

    @EventHandler
    public void signPlace(SignChangeEvent event)
    {
        if (event.getLine(0).equalsIgnoreCase("[FaceBook]") && module.clients.containsKey(event.getPlayer().getName()))
        {
            FacebookClient client = module.clients.get(event.getPlayer().getName());
            Page page = client.fetchObject(event.getLine(1), Page.class);
            event.getPlayer().sendMessage("Your page id is: "+page.getId()); //This id is actually valid
            pages.put(event.getBlock().getLocation(), page.getId());
        }
    }

    public void signInteract(PlayerInteractEvent event)
    {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
            FacebookClient client = module.clients.get(event.getPlayer().getName());
            Like likeResponse = client.fetchObject("me/og.like", Like.class, Parameter.with("object", ""));// should like the post the player posted with /facebook sign Message "message"
        }

    }

}
