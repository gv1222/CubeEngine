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
package de.cubeisland.engine.basics.command.moderation.kit;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.Transient;

import de.cubeisland.engine.basics.Basics;
import de.cubeisland.engine.configuration.YamlConfiguration;
import de.cubeisland.engine.configuration.annotations.Comment;
import de.cubeisland.engine.configuration.annotations.Name;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.util.time.Duration;

public class KitConfiguration extends YamlConfiguration
{
    @Comment("Players that join your server the first time will receive this kit if set on true.")
    @Name("give-on-first-join")
    public boolean giveOnFirstJoin = false;
    @Comment("If not empty this message will be displayed when receiving this kit.")
    @Name("custom-receive-message")
    public String customReceiveMsg = "";
    @Comment("amount*itemName/Id:Data customName\n"
        + "example: 64*STONE:0 MyFirstStoneBlocks")
    @Name("items")
    public List<KitItem> kitItems = new LinkedList<>();
    @Name("commands")
    public List<String> kitCommands = new LinkedList<>();
    @Comment("If a permission is generated the user needs the permission to bew able to receive this kit")
    @Name("generate-permission")
    public boolean usePerm = false;
    @Comment("The delay between each usage of this kit.")
    @Name("limit-usage-delay")
    public Duration limitUsageDelay = new Duration("-1");
    @Comment("Limits the usage to x amount. Use 0 for infinite.")
    @Name("limit-usage")
    public int limitUsage = 0;

    @Transient
    public String kitName;

    @Override
    public void onLoaded(File loadFrom)
    {
        this.kitName = StringUtils.stripFileExtension(this.getFile().getName());
        if (this.kitName.length() > 50)
        {
            this.kitName = this.kitName.substring(0, 50); // limit for db
        }
    }

    public Kit getKit(Basics module)
    {
        return new Kit(module, this.kitName, this.giveOnFirstJoin, this.limitUsage, this.limitUsageDelay.toMillis(), this.usePerm, this.customReceiveMsg, this.kitCommands, this.kitItems);
    }
}
