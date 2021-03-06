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
package de.cubeisland.engine.roles.commands;

import java.util.Set;

import org.bukkit.World;

import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.roles.Roles;
import de.cubeisland.engine.roles.role.Role;
import de.cubeisland.engine.roles.role.RolesAttachment;

public class UserManagementCommands extends UserCommandHelper
{
    public UserManagementCommands(Roles module)
    {
        super(module);
        this.registerAlias(new String[]{"manuser"},new String[]{});
    }

    @Alias(names = {"manuadd", "assignurole", "addurole", "giveurole"})
    @Command(names = {"assign", "add", "give"},
             desc = "Assign a role to the player [in world] [-temp]",
             usage = "<player> <role> [in <world>]",
             params = @Param(names = "in", type = World.class),
             flags = @Flag(name = "t",longName = "temp"),
             max = 2, min = 2)
    public void assign(ParameterizedContext context)
    {
        User user = this.getUser(context, 0);
        if (user == null) return;
        World world = this.getWorld(context);
        if (world == null) return;
        long worldId = this.worldManager.getWorldId(world);
        String roleName = context.getString(1);
        Role role = this.manager.getProvider(world).getRole(roleName);
        if (role == null)
        {
            context.sendTranslated("&eCould not find the role &6%s&e in &6%s&e.", roleName, world.getName());
            return;
        }
        if (!role.canAssignAndRemove(context.getSender()))
        {
            context.sendTranslated("&cYou are not allowed to assign the role &6%s&c in &6%s&c!",role.getName(),world.getName());
            return;
        }
        RolesAttachment attachment = this.manager.getRolesAttachment(user);
        if (context.hasFlag("t"))
        {
            if (!user.isOnline())
            {
                context.sendTranslated("&cYou cannot assign a temporary role to a offline player!");
                return;
            }
            if (attachment.getTemporaryRawData(worldId).assignRole(role))
            {
                attachment.reloadFromDatabase();
                attachment.apply();
                context.sendTranslated("&aAdded the role &6%s&a temporarily to &2%s&a in &6%s&a.", roleName, user.getName(), world.getName());
                return;
            }
            context.sendTranslated("&2%s&e already had the role &6%s&e in &6%s&e.", user.getName(), roleName, world.getName());
            return;
        }
        if (attachment.getRawData(worldId).assignRole(role))
        {
            attachment.reloadFromDatabase();
            attachment.apply();
            context.sendTranslated("&aAdded the role &6%s&a to &2%s&a in &6%s&a.", roleName, user.getName(), world.getName());
            return;
        }
        context.sendTranslated("&2%s&e already had the role &6%s&e in &6%s&e.", user.getName(), roleName, world.getName());
    }

    @Alias(names = {"remurole", "manudel"})
    @Command(desc = "Removes a role from the player [in world]",
             usage = "<player> <role> [in <world>]",
             params = @Param(names = "in", type = World.class),
             max = 2, min = 2)
    public void remove(ParameterizedContext context)
    {
        User user = this.getUser(context, 0);
        if (user == null) return;
        World world = this.getWorld(context);
        if (world == null) return;
        long worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        Role role = this.manager.getProvider(world).getRole(context.getString(1));
        if (role == null)
        {
            context.sendTranslated("&eCould not find the role &6%s &ein &6%s&e.", context.getString(1), world.getName());
            return;
        }
        if (!role.canAssignAndRemove(context.getSender()))
        {
            context.sendTranslated("&cYou are not allowed to remove the role &6%s&c in &6%s&c!",role.getName(),world.getName());
            return;
        }
        RolesAttachment attachment = this.manager.getRolesAttachment(user);
        if (attachment.getRawData(worldId).removeRole(role))
        {
            attachment.reloadFromDatabase();
            attachment.apply();
            context.sendTranslated("&aRemoved the role &6%s&a from &2%s&a in &6%s&a.", role.getName(), user.getName(), world.getName());
            return;
        }
        context.sendTranslated("&2%s&e did not have the role &6%s&e in &6%s&e.", user.getName(), role.getName(), world.getName());
    }

    @Alias(names = {"clearurole", "manuclear"})
    @Command(desc = "Clears all roles from the player and sets the defaultroles [in world]",
             usage = "<player> [in <world>]",
             params = @Param(names = "in", type = World.class),
             max = 1, min = 1)
    public void clear(ParameterizedContext context)
    {
        User user = this.getUser(context, 0);
        if (user == null) return;
        World world = this.getWorld(context);
        if (world == null) return;
        long worldId = this.getModule().getCore().getWorldManager().getWorldId(world);
        RolesAttachment attachment = this.manager.getRolesAttachment(user);
        attachment.getRawData(worldId).clearAssignedRoles();
        Set<Role> defaultRoles = this.manager.getProvider(worldId).getDefaultRoles();
        attachment.getTemporaryRawData(worldId).setAssignedRoles(defaultRoles);
        attachment.apply();
        context.sendTranslated("&eCleared the roles of &2%s&e in &6%s&e.", user.getName(), world.getName());
        if (!defaultRoles.isEmpty())
        {
            context.sendTranslated("&eDefault roles assigned:");
            for (Role role : defaultRoles)
            {
                context.sendMessage(String.format(this.LISTELEM,role.getName()));
            }
        }
    }

    @Command(names = {"setperm", "setpermission"},
             desc = "Sets a permission for this user [in world]",
             usage = " <player> <permission> <true|false|reset> [in <world>]",
             params = @Param(names = "in", type = World.class),
             max = 3, min = 3)
    public void setpermission(ParameterizedContext context)
    {
        User user = this.getUser(context, 0);
        if (user == null) return;
        String perm = context.getString(1);
        Boolean set;
        String setTo = context.getString(2);
        if (setTo.equalsIgnoreCase("true"))
        {
            set = true;
        }
        else if (setTo.equalsIgnoreCase("false"))
        {
            set = false;
        }
        else if (setTo.equalsIgnoreCase("reset"))
        {
            set = null;
        }
        else
        {
            context.sendTranslated("&cUnkown setting: &6%s &cUse &6true&c,&6false&c or &6reset&c!", setTo);
            return;
        }
        World world = this.getWorld(context);
        if (world == null) return;
        RolesAttachment attachment = this.manager.getRolesAttachment(user);
        attachment.getRawData(this.worldManager.getWorldId(world)).setPermission(perm,set);
        attachment.apply();
        if (set == null)
        {
            context.sendTranslated("&ePermission &6%s&e of &2%s&e resetted!", perm, user.getName());
            return;
        }
        if (set)
        {
            context.sendTranslated("&aPermission &6%s&a of &2%s&a set to true!", perm, user.getName());
            return;
        }
        context.sendTranslated("&cPermission &6%s&c of &2%s&c set to false!", perm, user.getName());
    }

    @Command(names = {"resetperm", "resetpermission"},
             desc = "Resets a permission for this user [in world]",
             usage = " <player> <permission> [in <world>]",
             params = @Param(names = "in", type = World.class),
             max = 2, min = 2)
    public void resetpermission(ParameterizedContext context)
    {
        User user = this.getUser(context, 0);
        if (user == null) return;
        String perm = context.getString(1);
        World world = this.getWorld(context);
        if (world == null) return;
        RolesAttachment attachment = this.manager.getRolesAttachment(user);
        attachment.getRawData(this.worldManager.getWorldId(world)).setPermission(perm,null);
        attachment.apply();
        context.sendTranslated("&ePermission &6%s&e of &2%s&e resetted!", perm, user.getName());
    }

    @Command(names = {"setdata", "setmeta", "setmetadata"},
             desc = "Sets metadata for this user [in world]",
             usage = "<player> <metaKey> <metaValue> [in <world>]",
             params = @Param(names = "in", type = World.class),
             max = 3, min = 3)
    public void setmetadata(ParameterizedContext context)
    {
        String metaKey = context.getString(1);
        String metaVal = context.getString(2);
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendTranslated("&cUser %s not found!", context.getString(0));
            return;
        }
        World world = this.getWorld(context);
        if (world == null) return;
        RolesAttachment attachment = this.manager.getRolesAttachment(user);
        attachment.getRawData(this.worldManager.getWorldId(world)).setMetadata(metaKey,metaVal);
        attachment.apply();
        context.sendTranslated("&aMetadata &6%s&a of &2%s&a set to &6%s&a in &6%s&a!", metaKey, user.getName(), metaVal, world.getName());
    }

    @Command(names = {"resetdata", "resetmeta", "resetmetadata", "deletedata", "deletemetadata", "deletemeta"},
             desc = "Resets metadata for this user [in world]",
             usage = "<player> <metaKey> [in <world>]",
             params = @Param(names = "in", type = World.class),
             max = 2, min = 2)
    public void resetmetadata(ParameterizedContext context)
    {
        String metaKey = context.getString(1);
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendTranslated("&cUser %s not found!", context.getString(0));
            return;
        }
        World world = this.getWorld(context);
        if (world == null) return;
        RolesAttachment attachment = this.manager.getRolesAttachment(user);
        attachment.getRawData(this.worldManager.getWorldId(world)).setMetadata(metaKey,null);
        attachment.apply();
        context.sendTranslated("&eMetadata &6%s&e of &2%s &eremoved in &6%s&e!", metaKey, user.getName(), world.getName());
    }

    @Command(names = {"cleardata", "clearmeta", "clearmetadata"},
             desc = "Resets metadata for this user [in world]",
             usage = "<player> [in <world>]",
             params = @Param(names = "in", type = World.class),
             max = 1, min = 1)
    public void clearMetaData(ParameterizedContext context)
    {
        User user = this.getUser(context, 0);
        if (user == null) return;
        World world = this.getWorld(context);
        if (world == null) return;
        RolesAttachment attachment = this.manager.getRolesAttachment(user);
        attachment.getRawData(this.worldManager.getWorldId(world)).clearMetadata();
        attachment.apply();
        context.sendTranslated("&eMetadata of &2%s &ecleared in &6%s&e!", user.getName(), world.getName());
    }
}
