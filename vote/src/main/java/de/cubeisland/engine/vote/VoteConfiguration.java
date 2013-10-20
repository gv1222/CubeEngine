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
package de.cubeisland.engine.vote;

import java.util.concurrent.TimeUnit;

import de.cubeisland.engine.configuration.YamlConfiguration;
import de.cubeisland.engine.configuration.annotations.Comment;
import de.cubeisland.engine.configuration.annotations.Name;
import de.cubeisland.engine.core.util.time.Duration;

public class VoteConfiguration extends YamlConfiguration
{
    @Name("vote-reward")
    public double votereward = 100.0;
    @Comment({"{PLAYER} will be replaced with the player-name",
             "{MONEY} will be replaced with the money the player receives",
             "{AMOUNT} will be replaced with the amount of times that player voted",
             "{VOTEURL} will be replaced with the configured vote-url"})
    @Name("vote-broadcast")
    public String votebroadcast = "&6{PLAYER} voted!";
    @Name("vote-message")
    public String votemessage = "&aYou received {MONEY} for voting {AMOUNT} times!";

    @Comment("Players will receive a bonus if they vote multiple times in given time-frame")
    @Name("vote-bonus-time")
    public Duration votebonustime = new Duration(TimeUnit.HOURS.toMillis(36));
    @Name("vote-url")
    public String voteurl = "";
}
