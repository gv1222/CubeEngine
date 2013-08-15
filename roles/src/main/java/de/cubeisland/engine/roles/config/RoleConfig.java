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
package de.cubeisland.engine.roles.config;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import de.cubeisland.engine.core.config.Configuration;
import de.cubeisland.engine.core.config.annotations.Codec;
import de.cubeisland.engine.core.config.annotations.Comment;
import de.cubeisland.engine.core.config.annotations.DefaultConfig;
import de.cubeisland.engine.core.config.annotations.Option;

@Codec("yml")
@DefaultConfig
public class RoleConfig extends Configuration
{
    @Option("role-name")
    @Comment("The name of this role")
    public String roleName = "defaultName";
    @Option("priority")
    @Comment("Use these as priority or just numbers\n"
        + "ABSULTEZERO(-273) < MINIMUM(0) < LOWEST(125) < LOWER(250) < LOW(375) < NORMAL(500) < HIGH(675) < HIGHER(750) < HIGHEST(1000) < OVER9000(9001)")
    public Priority priority = Priority.ABSULTEZERO;
    @Option("permissions")
    @Comment("The permission\n" +
                 "permissions nodes can be assigned individually e.g.:\n" +
                 " - cubeengine.roles.command.assign\n" +
                 "or grouped into a tree (this will be done automatically) like this:\n" +
                 " - cubeengine.roles:\n" +
                 "     - command.assign\n" +
                 "     - world.world:\n" +
                 "         - guest\n" +
                 "         - member\n" +
                 "Use - directly in front of a permission to revoke that permission e.g.:\n" +
                 " - -cubeengine.roles.command.assign")
    public PermissionTree perms = new PermissionTree();
    @Option("parents")
    @Comment("The roles this role will inherit from.\n"
        + "Any priority of parents will be ignored!")
    public Set<String> parents = new HashSet<>();
    @Option("metadata")
    @Comment("The metadata such as prefix or suffix e.g.:\n" +
                 "metadata: \n" +
                 "  prefix: '&7Guest'")
    public Map<String, String> metadata = new LinkedHashMap<>();

    @Override
    public void onLoaded(Path loadFrom) {
        if (this.priority == null)
        {
            this.priority = Priority.ABSULTEZERO;
        }
        if (this.parents == null)
        {
            this.parents = new HashSet<>();
        }
        if (this.metadata == null)
        {
            this.metadata = new LinkedHashMap<>();
        }
    }
}
