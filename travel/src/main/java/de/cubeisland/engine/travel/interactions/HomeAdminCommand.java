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
package de.cubeisland.engine.travel.interactions;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import de.cubeisland.engine.core.command.ArgBounds;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandResult;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.result.confirm.ConfirmResult;
import de.cubeisland.engine.core.permission.PermDefault;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.travel.Travel;
import de.cubeisland.engine.travel.storage.Home;
import de.cubeisland.engine.travel.storage.TelePointManager;
import de.cubeisland.engine.travel.storage.TeleportPointModel;

public class HomeAdminCommand extends ContainerCommand
{
    private final TelePointManager tpManager;
    private final Travel module;

    public HomeAdminCommand(Travel module)
    {
        super(module, "admin", "Teleport to another users home");
        this.module = module;
        this.tpManager = module.getTelepointManager();

        this.setUsage("[User] [Home]");
        this.getContextFactory().setArgBounds(new ArgBounds(0, 2));
    }

    @Override
    public CommandResult run(CommandContext context) throws Exception
    {
        if (context.isSender(User.class))
        {
            User sender = (User)context.getSender();
            User user = context.getUser(0);
            Home home;
            if (user == null)
            {
                sender.sendTranslated("&2%s&c is not an user on this server!", context.getString(0));
                return null;
            }

            if (context.getArgCount() == 2)
            {
                home = tpManager.getHome(user, context.getString(1));
                if (home == null)
                {
                    sender.sendTranslated("&2%s&c does not have a home named &6%s&c!", user.getName(), context.getString(1));
                    return null;
                }
            }
            else
            {
                home = tpManager.getHome(user, "home");
                if (home == null)
                {
                    sender.sendTranslated("&2%s&c does not have a home!", user.getName());
                    return null;
                }
            }
            Location location = home.getLocation();
            if (location == null)
            {
                context.sendTranslated("&cThis home is in a world that no longer exists!");
                return null;
            }
            sender.teleport(location, TeleportCause.COMMAND);
            if (home.getWelcomeMsg() != null)
            {
                sender.sendMessage(home.getWelcomeMsg());
            }
            else
            {
                sender.sendTranslated("&aYou have been teleported to &2%s&a's home!", user.getName());
            }
            return null;
        }
        else
        {
            return super.run(context);
        }
    }

    @Alias(names = {"clearhomes"})
    @Command(desc = "Clear all homes (of an user)",
             flags = {
        @Flag(name = "pub", longName = "public"),
        @Flag(name = "priv", longName = "Private")},
             permDefault =  PermDefault.OP, max = 1,
             usage = " <user> <-public> <-Private>")
    public ConfirmResult clear(final ParameterizedContext context)
    {
        if (context.getArgCount() > 0)
        {
            if (context.getUser(0) == null)
            {
                context.sendTranslated("&2%s &cIsn't an user on this server", context.getString(0));
                return null;
            }
            else
            {
                if (context.hasFlag("pub"))
                {
                    context.sendTranslated("&eAre you sure you want to delete all public homes ever created by &2%s?", context.getString(0));
                    context.sendTranslated("&eTo delete all the public homes, do: &6\"/confirm\" &ebefore 30 seconds has passed");
                }
                else if (context.hasFlag("priv"))
                {
                    context.sendTranslated("&eAre you sure you want to delete all private homes ever created by &2%s?", context.getString(0));
                    context.sendTranslated("&eTo delete all the private homes, do: &6\"/confirm\" &ebefore 30 seconds has passed");
                }
                else
                {
                    context.sendTranslated("&eAre you sure you want to delete all homes ever created by &2%s?", context.getString(0));
                    context.sendTranslated("&eTo delete all the homes, do: &6\"/confirm\" &ebefore 30 seconds has passed");
                }
            }
        }
        else
        {
            if (context.hasFlag("pub"))
            {
                context.sendTranslated("&eAre you sure you want to delete all public homes ever created on this server!?");
                context.sendTranslated("&eTo delete all the public homes of every user, do: &6\"/confirm\" &ebefore 30 seconds has passed");
            }
            else if (context.hasFlag("priv"))
            {
                context.sendTranslated("&eAre you sure you want to delete all private homes ever created on this server?");
                context.sendTranslated("&eTo delete all the private homes of every user, do: &6\"/confirm\" &ebefore 30 seconds has passed");
            }
            else
            {
                context.sendTranslated("&eAre you sure you want to delete all homes ever created on this server!?");
                context.sendTranslated("&eTo delete all the homes of every user, do: &6\"/confirm\" &ebefore 30 seconds has passed");
            }
        }
        return new ConfirmResult(new Runnable()
        {
            @Override
            public void run()
            {
                if (context.getArgCount() == 0)
                { // No user
                    int mask = context.getFlagCount() == 0 ? tpManager.ALL : 0;
                    if (context.hasFlag("pub"))
                    {
                        mask |= tpManager.PUBLIC;
                    }
                    if (context.hasFlag("priv"))
                    {
                        mask |= tpManager.PRIVATE;
                    }
                    tpManager.deleteHomes(mask);
                    context.sendTranslated("&aThe homes are now deleted");
                }
                else
                {
                    User user = context.getUser(0);
                    int mask = context.getFlagCount() == 0 ? tpManager.ALL : 0;
                    if (context.hasFlag("pub"))
                    {
                        mask |= tpManager.PUBLIC;
                    }
                    if (context.hasFlag("priv"))
                    {
                        mask |= tpManager.PRIVATE;
                    }
                    tpManager.deleteHomes(user, mask);
                    context.sendTranslated("&aThe homes are now deleted");
                }
            }
        }, context);
    }

    @Command(desc = "List all (public) homes", flags = {
        @Flag(name = "pub", longName = "public"),
        @Flag(name = "priv", longName = "private"),
        @Flag(name = "o", longName = "owned"),
        @Flag(name = "i", longName = "invited")
    }, permDefault =  PermDefault.OP, min = 0, max = 1, usage = " <<user>  <-owned> <-invited>> <-public> <-private>")
    public void list(ParameterizedContext context)
    {
        int mask = context.getFlagCount() == 0 ? tpManager.ALL : 0;
        if (context.hasFlag("pub"))
        {
            mask |= tpManager.PUBLIC;
        }
        if (context.hasFlag("priv"))
        {
            mask |= tpManager.PRIVATE;
        }
        if (context.hasFlag("o"))
        {
            mask |= tpManager.OWNED;
        }
        if (context.hasFlag("i"))
        {
            mask |= tpManager.INVITED;
        }

        Set<Home> homes;
        if (context.getArgCount() == 0)
        {
            homes = tpManager.listHomes(mask);
        }
        else
        {
            User user = context.getUser(0);
            if (user == null)
            {
                context.sendTranslated("&cCan't find any user named &2%s", context.getString(0));
                return;
            }
            homes = tpManager.listHomes(user, mask);
        }
        if (homes.isEmpty())
        {
            context.sendTranslated("&cThe query returned no homes!");
            return;
        }
        for (Home home : homes)
        {
            if (home.isPublic())
            {
                context.sendTranslated("  &2public&e:&6%s", home.getName());
            }
            else
            {
                context.sendTranslated("  &2%s&e:&6%s", home.getOwnerName(), home.getName());
            }
        }
    }

    @Command(names = {"private", "makeprivate"},
             permDefault =  PermDefault.OP,
             desc = "Make a users home private",
             min = 1, max = 1, usage = "<owner>:<home>")
    public void makePrivate(CommandContext context)
    {
        Home home;
        home = tpManager.getHome(context.getString(0));
        if (home == null)
        {
            context.sendTranslated("&cCouldn't find &6%s", context.getString(0));
            return;
        }
        if (!home.isPublic())
        {
            context.sendTranslated("&6%s&c is already private!", context.getString(0));
            return;
        }
        home.setVisibility(TeleportPointModel.VISIBILITY_PRIVATE);
        context.sendTranslated("&6%s&a is now private", context.getString(0));
    }

    @Command(names = {"public", "makepublic"},
             permDefault = PermDefault.OP,
             desc = "Make a users home public",
             min = 1, max = 1,
             usage = " owner:home")
    public void makePublic(CommandContext context)
    {
        Home home;
        home = tpManager.getHome(context.getString(0));
        if (home == null)
        {
            context.sendTranslated("&cCouldn't find &6%s", context.getString(0));
            return;
        }
        if (home.isPublic())
        {
            context.sendTranslated("&6%s &cis already public!", context.getString(0));
            return;
        }
        home.setVisibility(TeleportPointModel.VISIBILITY_PUBLIC);
        context.sendTranslated("&6%s&a is now public", context.getString(0));
    }
}
