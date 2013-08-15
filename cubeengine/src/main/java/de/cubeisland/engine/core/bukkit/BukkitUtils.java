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

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.logging.Filter;
import java.util.logging.Logger;

import net.minecraft.server.v1_6_R2.DedicatedPlayerList;
import net.minecraft.server.v1_6_R2.EntityPlayer;
import net.minecraft.server.v1_6_R2.Item;
import net.minecraft.server.v1_6_R2.RecipesFurnace;
import net.minecraft.server.v1_6_R2.TileEntityFurnace;
import org.bukkit.craftbukkit.v1_6_R2.CraftServer;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_6_R2.inventory.CraftItemStack;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.bukkit.packethook.PacketHookInjector;
import de.cubeisland.engine.core.i18n.I18n;
import de.cubeisland.engine.core.i18n.Language;
import de.cubeisland.engine.core.user.User;
import sun.misc.Signal;
import sun.misc.SignalHandler;



/**
 * This class contains various methods to access bukkit-related stuff.
 */
public class BukkitUtils
{
    private static Field entityPlayerLocaleField;

    static boolean init(BukkitCore core)
    {
        try
        {
            entityPlayerLocaleField = EntityPlayer.class.getDeclaredField("locale");
            entityPlayerLocaleField.setAccessible(true);

        }
        catch (Exception e)
        {
            core.getLog().error("Failed to initialize the required hacks!", e);
            return false;
        }
        return true;
    }

    private BukkitUtils()
    {}

    public static boolean isCompatible(BukkitCore core)
    {
        String serverClassName = core.getServer().getClass().getName();
        return (serverClassName.startsWith("org.bukkit.craftbukkit.") && serverClassName.endsWith(".CraftServer"));
    }

    public static Locale getLocaleFromSender(I18n i18n, CommandSender sender)
    {
        if (sender instanceof de.cubeisland.engine.core.command.CommandSender)
        {
            return ((de.cubeisland.engine.core.command.CommandSender)sender).getLocale();
        }
        Locale locale = null;
        if (sender instanceof Player)
        {
            locale = getLocaleFromUser(i18n, (Player)sender);
        }
        if (locale == null)
        {
            locale = Locale.getDefault();
        }
        return locale;
    }

    /**
     * Returns the locale string of a player.
     *
     * @param player the Player instance
     * @return the locale string of the player
     */
    private static Locale getLocaleFromUser(I18n i18n, Player player)
    {
        if (player.getClass() == CraftPlayer.class)
        {
            try
            {
                final String localeString = (String)entityPlayerLocaleField.get(((CraftPlayer)player).getHandle());
                final Language lang = i18n.getLanguage(I18n.stringToLocale(localeString));
                if (lang != null)
                {
                    return lang.getLocale();
                }
            }
            catch (Exception ignored)
            {}
        }
        return null;
    }

    public static CommandMap getCommandMap(final Server server)
    {
        return ((CraftServer)server).getCommandMap();
    }

    private static Filter filter = null;
    private static CommandLogFilter commandFilter = null;

    static void disableCommandLogging()
    {
        if (commandFilter == null)
        {
            commandFilter = new CommandLogFilter();
        }
        Logger logger = Bukkit.getLogger();
        filter = logger.getFilter();
        logger.setFilter(commandFilter);
    }

    static void resetCommandLogging()
    {
        if (commandFilter != null)
        {
            Logger logger = Bukkit.getLogger();
            if (logger.getFilter() == commandFilter)
            {
                logger.setFilter(filter);
            }
            filter = null;
        }
    }

    public static int getPing(Player onlinePlayer)
    {
        return ((CraftPlayer)onlinePlayer).getHandle().ping;
    }

    public static boolean isInvulnerable(Player player)
    {
        if (player != null)
        {
            if (player instanceof User)
            {
                player = ((User)player).getOfflinePlayer().getPlayer();
            }
            if (player != null && player instanceof CraftPlayer)
            {
                return ((CraftPlayer)player).getHandle().abilities.isInvulnerable;
            }
        }
        return false;
    }

    public static void setInvulnerable(Player player, boolean state)
    {
        if (player != null && player instanceof User)
        {
            player = ((User)player).getOfflinePlayer().getPlayer();
        }
        if (player != null && player instanceof CraftPlayer)
        {
            ((CraftPlayer)player).getHandle().abilities.isInvulnerable = state;
            ((CraftPlayer)player).getHandle().updateAbilities();
        }
    }

    public static synchronized void cleanup()
    {
        PacketHookInjector.shutdown();

        resetCommandLogging();
    }

    public static void setOnlineMode(boolean mode)
    {
        ((CraftServer)Bukkit.getServer()).getServer().setOnlineMode(mode);
        saveServerProperties();
    }

    public static void saveServerProperties()
    {
        ((CraftServer)Bukkit.getServer()).getServer().getPropertyManager().savePropertiesFile();
    }

    public static void wipeWhiteliste()
    {
        DedicatedPlayerList playerList = ((CraftServer)Bukkit.getServer()).getHandle();
        playerList.getWhitelisted().clear();
        // The method to write the whitelist (DedicatedPlayerList.w()) is private,
        // however removing an entry triggers the write :)
        playerList.removeWhitelist("");
    }

    /**
     * Returns true if given material is allowed to be placed in the top brewingstand slot
     *
     * @param material
     * @return
     */
    public static boolean canBePlacedInBrewingstand(Material material)
    {
        return Item.byId[material.getId()].x();
    }

    public static boolean isFuel(ItemStack item)
    {
        // Create an NMS item stack
        net.minecraft.server.v1_6_R2.ItemStack nmss = CraftItemStack.asNMSCopy(item);
        // Use the NMS TileEntityFurnace to check if the item being clicked is a fuel
        return TileEntityFurnace.isFuel(nmss);
    }

    public static boolean isSmeltable(ItemStack item)
    {
        net.minecraft.server.v1_6_R2.ItemStack nmss = CraftItemStack.asNMSCopy(item);
        // If the result of that item being cooked is null, it is not cookable
        return RecipesFurnace.getInstance().getResult(nmss.getItem().id) != null;
    }

    static void setSignalHandlers(final BukkitCore core)
    {
        try
        {
            Class.forName("sun.misc.Signal");

            Signal.handle(new Signal("INT"), new SignalHandler()
            {
                private long lastReceived = 0;

                @Override
                public void handle(Signal signal)
                {
                    if (this.lastReceived == -1)
                    {
                        return;
                    }
                    final long time = System.currentTimeMillis();
                    if (time - this.lastReceived <= 5000)
                    {
                        core.getLog().info("Shutting down the server now!");
                        core.getServer().shutdown();
                        this.lastReceived = -1;
                    }
                    else
                    {
                        this.lastReceived = time;
                        core.getLog().info("You can't copy content from the console using CTRL-C!");
                        core.getLog().info("If you really want shutdown the server use the stop command or press CTRL-C again within 5 seconds!");
                    }
                }
            });

            try
            {
                Signal.handle(new Signal("HUP"), new SignalHandler() {
                    private volatile boolean reloading = false;

                    @Override
                    public void handle(Signal signal)
                    {
                        if (!this.reloading)
                        {
                            this.reloading = true;
                            core.getLog().info("Reloading the server!");
                            core.getServer().reload();
                            core.getLog().info("Done reloading the server!");
                            this.reloading = false;
                        }
                    }
                });
            }
            catch (IllegalArgumentException e)
            {
                core.getLog().info("You're OS does not support the HUP signal! This can be ignored.");
            }
        }
        catch (ClassNotFoundException ignored)
        {}
    }

    public static boolean isANSISupported()
    {
        return ((CraftServer) Bukkit.getServer()).getReader().getTerminal().isAnsiSupported();
    }
}
