package de.cubeisland.cubeengine.social.interactions;

import de.cubeisland.cubeengine.social.Social;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
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

    public void signInteract(PlayerInteractEvent event)
    {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && (event.getClickedBlock().getType() == Material.SIGN || event.getClickedBlock().getType() == Material.WALL_SIGN))
        {
            // TODO like the post associated with the sign
        }

    }

}
