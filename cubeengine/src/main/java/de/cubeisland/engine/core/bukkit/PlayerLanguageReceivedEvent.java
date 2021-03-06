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

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * The event gets called when ever a player receives the packet 204.
 * This is the case right after the player joins the server
 * and when the user changes his language or view distance settings
 */
public class PlayerLanguageReceivedEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();
    private final String language;
    private final Player player;

    public PlayerLanguageReceivedEvent(Player player, String language)
    {
        this.language = language;
        this.player = player;
    }

    /**
     * Returns the player of this event
     *
     * @return the player
     */
    public Player getPlayer()
    {
        return this.player;
    }

    /**
     * Returns the locale string of the player
     *
     * @return the locale string
     */
    public String getLanguage()
    {
        return this.language;
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
