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

import de.cubeisland.engine.configuration.YamlConfiguration;
import de.cubeisland.engine.configuration.annotations.Comment;
import de.cubeisland.engine.configuration.annotations.Name;

public class ChatConfig extends YamlConfiguration
{
    @Comment("There at least the following variables available:\n- {NAME} -> player name\n- {DISPLAY_NAME} -> display name\n- {WORLD} -> the world the player is in\n- {MESSAGE} -> the message\n\nUsual color/format codes are also supported: &1, ... &f, ... &r")
    public String format = "{NAME}: {MESSAGE}";

    @Comment("This also counts for the format string!")
    public boolean allowColors = true;
}
