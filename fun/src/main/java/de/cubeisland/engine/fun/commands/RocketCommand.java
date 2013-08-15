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
package de.cubeisland.engine.fun.commands;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserManager;
import de.cubeisland.engine.fun.Fun;

public class RocketCommand
{
    private final Fun module;
    private final RocketListener rocketListener;

    public RocketCommand(Fun module)
    {
        this.module = module;
        this.rocketListener = new RocketListener();
        module.getCore().getEventManager().registerListener(module, rocketListener);
    }

    public RocketListener getRocketListener()
    {
        return this.rocketListener;
    }

    @Command(desc = "rockets a player", max = 1, usage = "[height] [player <name>]", params = {
        @Param(names = {
            "player", "p"
        }, type = User.class)
    })
    public void rocket(ParameterizedContext context)
    {
        int height = context.getArg(0, Integer.class, 10);
        User user = null;
        if (context.hasParam("player"))
        {
            user = context.getParam("player");
            if (user == null)
            {
                context.sendTranslated("&cThe given user was not found!");
                return;
            }
        }
        else
        {
            if (!(context.getSender() instanceof User))
            {
                context.sendTranslated("&cYou have to specify a user!");
                return;
            }
            user = (User)context.getSender();
        }

        if (user == null)
        {
            context.sendTranslated("&cUser not found!");
            return;
        }

        if (height > this.module.getConfig().maxRocketHeight)
        {
            context.sendTranslated("&cDo you never wanna see %s again?", user.getName());
            return;
        }
        else if (height < 0)
        {
            context.sendTranslated("&cThe height has to be greater than 0");
            return;
        }

        rocketListener.addInstance(user, height);
    }

    public class RocketListener implements Listener, Runnable
    {
        private final UserManager userManager;
        private final Set<RocketCMDInstance> instances;
        private int taskId = -1;
        private final Location helper = new Location(null, 0, 0, 0);

        public RocketListener()
        {
            this.userManager = module.getCore().getUserManager();
            this.instances = new HashSet<>();
        }

        public void addInstance(User user, int height)
        {
            if (!this.contains(user))
            {
                instances.add(new RocketCMDInstance(user.getName(), height));

                if (taskId == -1)
                {
                    this.taskId = module.getCore().getTaskManager().runTimer(module, this, 0, 2);
                }
            }
        }

        public Collection<User> getUsers()
        {
            Set<User> users = new HashSet<>();
            for (RocketCMDInstance instance : instances)
            {
                users.add(instance.getUser());
            }
            return users;
        }

        public boolean contains(User user)
        {
            for (User users : this.getUsers())
            {
                if (users.getName().equals(user.getName()))
                {
                    return true;
                }
            }
            return false;
        }

        public void removeInstance(User user)
        {
            RocketCMDInstance trash = null;
            
            for (RocketCMDInstance instance : instances)
            {
                if (instance.getName().equals(user.getName()))
                {
                    trash = instance;
                    break;
                }
            }
            
            if(trash != null)
            {
                this.instances.remove(trash);

                if (instances.isEmpty())
                {
                    module.getCore().getTaskManager().cancelTask(module, taskId);
                    taskId = -1;
                }
            }
        }

        public Collection<RocketCMDInstance> getInstances()
        {
            return this.instances;
        }

        @EventHandler
        public void onEntityDamage(EntityDamageEvent event)
        {
            if (event.getEntity() instanceof Player && event.getCause() == EntityDamageEvent.DamageCause.FALL)
            {
                User user = this.userManager.getExactUser(((Player)event.getEntity()).getName());
                if (user == null)
                {
                    return;
                }

                if (this.contains(user))
                {
                    event.setCancelled(true);
                    this.removeInstance(user);
                }
            }
        }

        @Override
        public void run()
        {
            for (RocketCMDInstance instance : this.getInstances())
            {
                final User user = instance.getUser();

                if (user.isOnline())
                {
                    if (!instance.getDown())
                    {
                        Location userLocation = user.getLocation(this.helper);
                        user.getWorld().playEffect(userLocation, Effect.SMOKE, 0);
                        user.getWorld().playEffect(userLocation.add(1, 0, 0), Effect.SMOKE, 0);
                        user.getWorld().playEffect(userLocation.add(-1, 0, 0), Effect.SMOKE, 0);
                        user.getWorld().playEffect(userLocation.add(0, 0, 1), Effect.SMOKE, 0);
                        user.getWorld().playEffect(userLocation.add(0, 0, -1), Effect.SMOKE, 0);
                    }

                    if (instance.getNumberOfAirBlocksUnderFeet() == 0 && instance.getDown())
                    {
                        module.getCore().getTaskManager().runTaskDelayed(module, new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                removeInstance(user);
                            }
                        }, 1);
                    }

                    if (instance.getNumberOfAirBlocksUnderFeet() < instance.getHeight() && instance.getNumberOfAirBlocksOverHead() > 2 && !instance.getDown())
                    {
                        double y = (double)(instance.getHeight() - instance.getNumberOfAirBlocksUnderFeet()) / 10;
                        y = (y < 10) ? y : 10;
                        user.setVelocity(new Vector(0, (y < 9) ? (y + 1) : y, 0));
                    }
                    else if (!instance.getDown())
                    {
                        instance.setDown();
                    }
                }
                else
                {
                    this.removeInstance(user);
                }
            }
        }

        private class RocketCMDInstance
        {
            private final String name;
            private final int height;
            private boolean down;

            private RocketCMDInstance(String name, int height)
            {
                this.name = name;
                this.height = height;
                this.down = false;
            }

            public void setDown()
            {
                this.down = true;
            }

            public boolean getDown()
            {
                return this.down;
            }

            public int getHeight()
            {
                return this.height;
            }

            public User getUser()
            {
                return CubeEngine.getUserManager().getUser(name, false);
            }

            public String getName()
            {
                return this.name;
            }

            public int getNumberOfAirBlocksOverHead()
            {
                final User user = this.getUser();
                if (user == null)
                {
                    return 0;
                }
                final Location location = this.getUser().getLocation();
                if (location == null)
                {
                    return 0;
                }
                location.add(0, 1, 0);
                int numberOfAirBlocks = 0;

                while (!location.getBlock().getType().isSolid() && location.getY() < location.getWorld().getMaxHeight())
                {
                    numberOfAirBlocks++;
                    location.add(0, 1, 0);
                }

                return numberOfAirBlocks;
            }

            public int getNumberOfAirBlocksUnderFeet()
            {
                final User user = this.getUser();
                if (user == null)
                {
                    return 0;
                }
                final Location location = this.getUser().getLocation();
                if (location == null)
                {
                    return 0;
                }
                location.subtract(0, 1, 0);
                int numberOfAirBlocks = 0;

                while (!location.getBlock().getType().isSolid() || location.getY() > location.getWorld().getMaxHeight())
                {
                    numberOfAirBlocks++;
                    location.subtract(0, 1, 0);
                }

                return numberOfAirBlocks;
            }
        }
    }
}
