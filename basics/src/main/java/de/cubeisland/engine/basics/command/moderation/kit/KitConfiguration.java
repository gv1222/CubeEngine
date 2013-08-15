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

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import de.cubeisland.engine.basics.Basics;
import de.cubeisland.engine.core.config.YamlConfiguration;
import de.cubeisland.engine.core.config.annotations.Comment;
import de.cubeisland.engine.core.config.annotations.Option;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.util.time.Duration;

public class KitConfiguration extends YamlConfiguration
{
    public String kitName;
    @Comment("Players that join your server the first time will receive this kit if set on true.")
    @Option("give-on-first-join")
    public boolean giveOnFirstJoin = false;
    @Comment("If not empty this message will be displayed when receiving this kit.")
    @Option("custom-receive-message")
    public String customReceiveMsg = "";
    @Comment("amount*itemName/Id:Data customName\n"
        + "example: 64*1:0 MyFirstStoneBlocks")
    @Option("items")
    public List<KitItem> kitItems = new LinkedList<>();
    @Option("commands")
    public List<String> kitCommands = new LinkedList<>();
    @Comment("If a permission is generated the user needs the permission to bew able to receive this kit")
    @Option("generate-permission")
    public boolean usePerm = false;
    @Comment("The delay between each usage of this kit.")
    @Option("limit-usage-delay")
    public Duration limitUsageDelay = new Duration("-1");
    @Comment("Limits the usage to x amount. Use 0 for infinite.")
    @Option("limit-usage")
    public int limitUsage = 0;

    @Override
    public void onLoaded(Path loadFrom)
    {
        this.kitName = StringUtils.stripFileExtension(this.getPath().getFileName().toString());
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
