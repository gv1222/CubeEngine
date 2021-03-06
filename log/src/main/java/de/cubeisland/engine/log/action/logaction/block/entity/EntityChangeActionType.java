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
package de.cubeisland.engine.log.action.logaction.block.entity;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import de.cubeisland.engine.log.action.logaction.ActionTypeContainer;
import de.cubeisland.engine.log.action.logaction.block.BlockActionType;
import de.cubeisland.engine.log.action.logaction.block.BlockActionType.BlockData;

import static org.bukkit.Material.AIR;

/**
 * Container-ActionType for entities changing blocks.
 * <p>Events: {@link EntityChangeBlockEvent}</p>
 * <p>External Actions: {@link SheepEat}, {@link EndermanPickup}, {@link EndermanPlace},
 */
public class EntityChangeActionType extends ActionTypeContainer
{
    public EntityChangeActionType()
    {
        super("ENTITY_CHANGE");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityChangeBlock(final EntityChangeBlockEvent event)
    {
        if(event.getEntityType().equals(EntityType.SHEEP))
        {
            this.logEntityChangeBlock(this.manager.getActionType(SheepEat.class),event.getBlock().getState(),event.getTo());
        }
        else if(event.getEntity() instanceof Enderman)
        {
            if (event.getTo().equals(AIR))
            {
                this.logEntityChangeBlock(this.manager.getActionType(EndermanPickup.class),
                                          event.getBlock().getState(), event.getTo());
            }
            else
            {
                this.logEntityChangeBlock(this.manager.getActionType(EndermanPlace.class),event.getBlock().getState(),event.getTo());
            }
        }
    }

    private void logEntityChangeBlock(BlockActionType actionType, BlockState from, Material to)
    {
        if (actionType.isActive(from.getWorld()))
        {
            actionType.logBlockChange(from.getLocation(),null, BlockData.of(from),to,null);
        }
    }

}
