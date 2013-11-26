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
package de.cubeisland.engine.reputation.privilige;

import de.cubeisland.engine.core.user.User;

public class CommandPrivilege implements Privilege
{
    private String commandGive;
    private String commandRemove;

    @Override
    public void give(User user)
    {
        if (this.commandGive != null)
        {
            String cmd = commandGive.replace("{USER}", user.getName());
            user.getCore().getCommandManager().runCommand(user.getCore().getCommandManager().getConsoleSender(), cmd);
        }
    }

    @Override
    public void remove(User user)
    {
        if (this.commandRemove != null)
        {
            String cmd = commandRemove.replace("{USER}", user.getName());
            user.getCore().getCommandManager().runCommand(user.getCore().getCommandManager().getConsoleSender(), cmd);
        }
    }
}
