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
package de.cubeisland.engine.log.action.logaction.worldedit;

import de.cubeisland.engine.log.Log;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSessionFactory;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.bukkit.BukkitWorld;

public class LogEditSessionFactory extends EditSessionFactory
{
    private final Log module;
    private final EditSessionFactory oldFactory;

    public LogEditSessionFactory(Log module, EditSessionFactory oldFactory)
    {
        this.module = module;
        this.oldFactory = oldFactory;
    }

    public static void initialize(WorldEdit worldEdit, Log module)
    {
        worldEdit.setEditSessionFactory(new LogEditSessionFactory(module, worldEdit.getEditSessionFactory()));
    }

    private boolean ignoreWorldEdit(LocalWorld world)
    {
        return world instanceof BukkitWorld && !this.module.getActionTypeManager().getActionType(WorldEditActionType.class).isActive(((BukkitWorld)world).getWorld());
    }

    @Override
    public EditSession getEditSession(LocalWorld world, int maxBlocks, LocalPlayer player)
    {
        if (this.ignoreWorldEdit(world))
        {
            return this.oldFactory.getEditSession(world, maxBlocks, player);
        }
        else
        {
            return new LogEditSession(world, maxBlocks, player, this.module);
        }
    }

    @Override
    public EditSession getEditSession(LocalWorld world, int maxBlocks, BlockBag blockBag, LocalPlayer player)
    {
        if (this.ignoreWorldEdit(world))
        {
            return this.oldFactory.getEditSession(world, maxBlocks, blockBag, player);
        }
        else
        {
            return new LogEditSession(world, maxBlocks, blockBag, player, this.module);
        }
    }

    @Override
    public EditSession getEditSession(LocalWorld world, int maxBlocks)
    {
        if (this.ignoreWorldEdit(world))
        {
            return this.oldFactory.getEditSession(world, maxBlocks);
        }
        else
        {
            return new LogEditSession(world, maxBlocks, this.module);
        }
    }

    @Override
    public EditSession getEditSession(LocalWorld world, int maxBlocks, BlockBag blockBag)
    {
        if (this.ignoreWorldEdit(world))
        {
            return this.oldFactory.getEditSession(world, maxBlocks, blockBag);
        }
        else
        {
            return new LogEditSession(world, maxBlocks, blockBag, this.module);
        }
    }

}
