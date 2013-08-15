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
package de.cubeisland.engine.core.bukkit;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.minecraft.server.v1_6_R2.RegionFileCache;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.filesystem.FileUtil;
import de.cubeisland.engine.core.world.AbstractWorldManager;
import de.cubeisland.engine.core.world.WorldEntity;
import gnu.trove.set.hash.THashSet;
import org.jooq.DSLContext;
import org.jooq.Result;

import static de.cubeisland.engine.core.world.TableWorld.TABLE_WORLD;

public class BukkitWorldManager extends AbstractWorldManager
{
    private final Server server;

    public BukkitWorldManager(final BukkitCore core)
    {
        super(core);
        this.server = core.getServer();

        core.addInitHook(new Runnable() {
            @Override
            public void run()
            {
                DSLContext dsl = database.getDSL();
                Result<WorldEntity> worldEntities = dsl.selectFrom(TABLE_WORLD).fetch();
                List<World> loadedWorlds = server.getWorlds();
                for (WorldEntity entity : worldEntities)
                {
                    World world = server.getWorld(entity.getWorldUUID());
                    if (loadedWorlds.contains(world))
                    {
                        loadedWorlds.remove(world);
                        worlds.put(world.getName(), entity);
                        worldIds.put(entity.getKey().longValue(), world);
                    }
                }
                if (!loadedWorlds.isEmpty()) // new worlds?
                {
                    for (World world : loadedWorlds)
                    {
                        WorldEntity entity = dsl.newRecord(TABLE_WORLD).newWorld(world);
                        entity.insert();
                        worlds.put(world.getName(), entity);
                        worldIds.put(entity.getKey().longValue(), world);
                    }
                }
            }
        });
    }

    void loadWorlds()
    {
    }

    public World createWorld(WorldCreator creator)
    {
        assert CubeEngine.isMainThread() : "Must be executed from main thread!";

        return this.server.createWorld(creator);
    }

    @Override
    public World getWorld(String name)
    {
        assert CubeEngine.isMainThread() : "Must be executed from main thread!";

        return this.server.getWorld(name);
    }

    @Override
    public World getWorld(UUID uid)
    {
        assert CubeEngine.isMainThread() : "Must be executed from main thread!";

        return this.server.getWorld(uid);
    }

    @Override
    public boolean unloadWorld(World world, boolean save)
    {
        assert CubeEngine.isMainThread() : "Must be executed from main thread!";
        boolean success = this.server.unloadWorld(world, save);
        if (success && !save)
        {
            RegionFileCache.a();
        }
        return success;
    }

    @Override
    public boolean deleteWorld(World world) throws IOException
    {
        if (world == null)
        {
            return false;
        }
        if (!this.unloadWorld(world, false))
        {
            return false;
        }
        // TODO this sometimes throws java.nio.file.FileSystemException when world-files are still used WHY??? should be unloaded
        // a Stacktrace: http://pastie.org/private/2xxfktrhnr3r7dw3vc42ua
        FileUtil.deleteRecursive(world.getWorldFolder().toPath());
        return true;
    }

    @Override
    public Set<World> getWorlds()
    {
        assert CubeEngine.isMainThread() : "Must be executed from main thread!";

        return new THashSet<>(this.server.getWorlds());
    }
}
