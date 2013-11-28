package de.cubeisland.engine.core.recipe.result.item;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.recipe.result.logic.Result;

public class DropResult extends Result
{
    private DropResult()
    {}

    @Override
    public ItemStack getResult(Player player, BlockState block, ItemStack itemStack)
    {
        if (block == null && player == null)
        {
            throw new IllegalArgumentException("DropResult without Player or Block!");
        }
        Location location;
        if (block != null)
        {
            location = block.getLocation();
        }
        else
        {
            location = player.getLocation();
        }
        location.getWorld().dropItemNaturally(location, itemStack);
        return new ItemStack(Material.AIR);
    }

    public static DropResult drop()
    {
        return new DropResult();
    }
}
