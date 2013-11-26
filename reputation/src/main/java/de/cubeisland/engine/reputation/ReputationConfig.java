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
package de.cubeisland.engine.reputation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.cubeisland.engine.configuration.YamlConfiguration;
import de.cubeisland.engine.configuration.annotations.Comment;
import de.cubeisland.engine.reputation.modifiers.Modifier;
import de.cubeisland.engine.reputation.privilige.Privilege;

public class ReputationConfig extends YamlConfiguration
{
    // TODO converters
    @Comment("Maps reputation to the privileges you get or loose when passing that reputation")
    public Map<Integer, Privilege> privileges = new HashMap<>();
    @Comment("A List of modifiers that can modify Reputation")
    public List<Modifier> modifiers = new ArrayList<>();

    // TODO report / praise? cmds values

    @Override
    public String[] head()
    {
        // TODO append all possible Modifiers & Privileges
        return null;
    }
}
