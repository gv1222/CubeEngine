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

import de.cubeisland.engine.configuration.annotations.Comment;
import de.cubeisland.engine.configuration.annotations.Name;
import de.cubeisland.engine.core.CoreConfiguration;

public class BukkitCoreConfiguration extends CoreConfiguration
{
    @Comment("Whether to prevent Bukkit from kicking players for spamming")
    public boolean preventSpamKick = false;

    public BukkitCommandsSection commands;

    public class BukkitCommandsSection extends CommandsSection
    {
        @Comment("Whether to replace the vanilla standard commands with improved ones")
        public boolean improveVanilla = true;
    }

    @Comment("This allows the CubeEngine to act when signals are send to the Minecraft server")
    public boolean catchSystemSignals = true;
}
