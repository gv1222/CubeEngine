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
package de.cubeisland.engine.stats.stat;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.stats.StatsManager;
import de.cubeisland.engine.stats.annotations.Configured;

public class PlayTimeStat extends Stat
{

    private Map<String, Long> joined;

    public PlayTimeStat(StatsManager manager, Module owner)
    {
        super(manager, owner);
    }

    public void onActivate()
    {
        this.joined = new HashMap<>();
    }

    @EventHandler()
    public void playerJoin(PlayerJoinEvent event)
    {
        joined.put(event.getPlayer().getName(), System.currentTimeMillis());
    }

    @EventHandler
    public void playerLeave(PlayerQuitEvent event)
    {
        long playtime = System.currentTimeMillis() - joined.get(event.getPlayer().getName());
        this.save(playtime);
    }
}
