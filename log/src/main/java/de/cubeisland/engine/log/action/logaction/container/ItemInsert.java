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
package de.cubeisland.engine.log.action.logaction.container;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.LogAttachment;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.engine.log.storage.ItemData;
import de.cubeisland.engine.log.storage.LogEntry;

import static de.cubeisland.engine.log.action.ActionTypeCategory.*;

/**
 * Inserting items into a container
 * <p>Events: {@link ContainerActionType}
 */
public class ItemInsert extends SimpleLogActionType
{
    @Override
    protected Set<ActionTypeCategory> getCategories()
    {
        return new HashSet<>(Arrays.asList(PLAYER, INVENTORY, ITEM));
    }

    @Override
    public String getName()
    {
        return "item-insert";
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        ItemData itemData= logEntry.getItemData();
        int amount = itemData.amount;
        if (logEntry.hasAttached())
        {
            for (LogEntry entry : logEntry.getAttached())
            {
                amount += entry.getItemData().amount;
            }
        }
        if (amount > 0)
        {
            user.sendTranslated("%s&2%s&a placed &6%d %s&a into &6%s%s",
                                time, logEntry.getCauserUser().getName(),
                                amount,itemData,
                                logEntry.getContainerTypeFromBlock(),loc);
        }
        else if (amount < 0)
        {
            user.sendTranslated("%s&2%s&a took &6%d %s&a out of &6%s%s",
                                time, logEntry.getCauserUser().getName(),
                                -amount,itemData,
                                logEntry.getContainerTypeFromBlock(),loc);
        }
        else
        {
            user.sendTranslated("%s&2%s&a did not change the amount of &6%s&a in &6%s%s",
                                time , logEntry.getCauserUser().getName(), itemData,
                                logEntry.getContainerTypeFromBlock(), loc);
        }
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return ContainerActionType.isSubActionSimilar(logEntry,other);
    }

    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).ITEM_INSERT_enable;
    }

    @Override
    public boolean rollback(LogAttachment attachment, LogEntry logEntry, boolean force, boolean preview)
    {
        Location loc = logEntry.getLocation();
        Material material = logEntry.getContainerTypeFromBlock().getMaterial();
        if (material.equals(Material.STORAGE_MINECART))
        {
            // TODO MinecartInventoryHolders  // would need UUID
        }
        else
        {
            BlockState block = loc.getBlock().getState();
            if (block instanceof InventoryHolder && block.getType().equals(material)) // Same container
            {
                ItemData itemData = logEntry.getItemData();
                InventoryHolder holder = (InventoryHolder)block;
                HashMap<Integer,ItemStack> couldNotRemove = holder.getInventory().removeItem(itemData.toItemStack());
                if (!couldNotRemove.isEmpty())
                {
                    attachment.getHolder().sendTranslated("&cCould not rollback an item-insert!");
                    return false;
                }
                return true;
            }
            if (force)
            {
                attachment.getHolder().sendTranslated("&cInvalid Container to rollback item-insert!");
            }
            return false;
        }
        return false;
    }
    // TODO furnace Minecart different event? / couldnt rollback that

    @Override
    public boolean canRollback()
    {
        return true;
    }

    @Override
    public boolean isBlockBound()
    {
        return true;
    }
}
