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
package de.cubeisland.engine.chat;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ChatFormatEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final String message;
    private final String format;
    final Map<String, String> variables;

    public ChatFormatEvent(Player player, String message, String format, boolean async)
    {
        super(async);
        this.player = player;
        this.message = message;
        this.format = format;
        this.variables = new HashMap<>();
    }

    public Player getPlayer()
    {
        return player;
    }

    public String getMessage()
    {
        return message;
    }

    public String getFormat()
    {
        return this.format;
    }

    public void setVariable(String name, String value)
    {
        if (name == null)
        {
            return;
        }
        name = name.toUpperCase(Locale.ENGLISH);
        if (value == null)
        {
            this.variables.remove(name);
        }
        else
        {
            this.variables.put(name, value);
        }
    }

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }
}
