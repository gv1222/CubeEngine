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
package de.cubeisland.engine.core.user;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.material.Step;
import org.bukkit.material.WoodenStep;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.BlockIterator;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.attachment.AttachmentHolder;
import de.cubeisland.engine.core.ban.IpBan;
import de.cubeisland.engine.core.ban.UserBan;
import de.cubeisland.engine.core.bukkit.BukkitCore;
import de.cubeisland.engine.core.bukkit.BukkitUtils;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.i18n.Language;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.util.BlockUtil;
import de.cubeisland.engine.core.util.ChatFormat;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import static de.cubeisland.engine.core.user.TableUser.TABLE_USER;
import static de.cubeisland.engine.core.util.BlockUtil.isInvertedStep;

/**
 * A CubeEngine User (can exist offline too).
 */
public class User extends UserBase implements CommandSender, AttachmentHolder<UserAttachment>
{
    private UserEntity entity;

    boolean loggedInState = false;
    private final Map<Class<? extends UserAttachment>, UserAttachment> attachments;
    private final Core core;

    User(Core core, String playerName)
    {
        super(playerName);
        this.entity = core.getDB().getDSL().newRecord(TABLE_USER).newUser(playerName);
        this.attachments = new THashMap<>();
        this.core = core;
    }

    User(Core core, OfflinePlayer player)
    {
        this(core, player.getName());
    }

    public User(UserEntity entity)
    {
        super(entity.getPlayer());
        this.core = CubeEngine.getCore();
        this.entity = entity;
        this.attachments = new THashMap<>();
    }

    public Core getCore()
    {
        return this.core;
    }

    @Override
    public synchronized <A extends UserAttachment> A attach(Class<A> type, Module module)
    {
        try
        {
            A attachment = type.newInstance();
            attachment.attachTo(module, this);
            @SuppressWarnings("unchecked")
            A oldAttachment = (A) this.attachments.put(type, attachment);
            if (oldAttachment != null)
            {
                oldAttachment.onDetach();
            }
            return attachment;
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("The given attachment could not be created!", e);
        }
    }

    @Override
    public synchronized <A extends UserAttachment> A attachOrGet(Class<A> type, Module module)
    {
        A attachment = this.get(type);
        if (attachment == null)
        {
            attachment = this.attach(type, module);
        }
        return attachment;
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized <A extends UserAttachment> A get(Class<A> type)
    {
        return (A)this.attachments.get(type);
    }

    public synchronized Set<UserAttachment> getAll()
    {
        return new THashSet<>(this.attachments.values());
    }

    @Override
    public synchronized <A extends UserAttachment> boolean has(Class<A> type)
    {
        return this.attachments.containsKey(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized <A extends UserAttachment> A detach(Class<A> type)
    {
        A attachment = (A)this.attachments.remove(type);
        if (attachment != null)
        {
            attachment.onDetach();
        }
        return attachment;
    }

    @Override
    public synchronized void detachAll(Module module)
    {
        final Iterator<Entry<Class<? extends UserAttachment>, UserAttachment>> it = this.attachments.entrySet().iterator();
        UserAttachment attachment;
        while (it.hasNext())
        {
            attachment = it.next().getValue();
            if (attachment.getModule() == module)
            {
                attachment.onDetach();
                it.remove();
            }
        }
    }

    @Override
    public synchronized void detachAll()
    {
        final Iterator<Entry<Class<? extends UserAttachment>, UserAttachment>> it = this.attachments.entrySet().iterator();
        while (it.hasNext())
        {
            it.next().getValue().onDetach();
            it.remove();
        }
    }

    public Long getId()
    {
        return this.entity.getKey().longValue();
    }

    @Override
    public void sendMessage(String string)
    {
        if (string == null)
        {
            return;
        }
        if (!Thread.currentThread().getStackTrace()[1].getClassName().equals(this.getClass().getName()))
        {
            CubeEngine.getLog().debug("A module sent an untranslated message!");
        }
        super.sendMessage(ChatFormat.parseFormats(string));
    }

    @Override
    public String translate(String message, Object... params)
    {
        return this.core.getI18n().translate(this.getLocale(), message, params);
    }

    /**
     * Sends a translated Message to this User
     *
     * @param message the message to translate
     * @param params optional parameter
     */
    @Override
    public void sendTranslated(String message, Object... params)
    {
        this.sendMessage(this.translate(message, params));
    }

    @Override
    public boolean isAuthorized(de.cubeisland.engine.core.permission.Permission perm)
    {
        return this.hasPermission(perm.getName());
    }

    /**
     * Returns the users configured language
     *
     * @return a locale string
     */
    public Locale getLocale()
    {
        if (this.entity.getLocale() != null)
        {
            return this.entity.getLocale();
        }
        Language language = null;
        Player onlinePlayer = this.getOfflinePlayer().getPlayer();
        if (onlinePlayer != null)
        {
            language = this.core.getI18n().getLanguage(
                BukkitUtils.getLocaleFromSender(this.core.getI18n(), onlinePlayer));
        }
        if (language == null)
        {
            language = this.core.getI18n().getDefaultLanguage();
        }
        return language.getLocale();
    }

    public void setLocale(Locale locale)
    {
        if (locale == null)
        {
            throw new NullPointerException();
        }
        this.entity.setLocale(locale);
    }

    public int getPing()
    {
        Player onlinePlayer = this.getOfflinePlayer().getPlayer();
        if (onlinePlayer == null)
        {
            return BukkitUtils.getPing(onlinePlayer);
        }
        else
        {
            return -1;
        }
    }

    @Override
    public long getLastPlayed()
    {
        if (this.isOnline())
        {
            return 0;
        }
        return this.entity.getLastseen().getTime();
    }

    public void safeTeleport(Location location, TeleportCause cause, boolean keepDirection)
    {
        Location checkLocation = location.clone().add(0, 1, 0);
        // Search for 2 non occluding blocks
        while (location.getBlock().getType().isSolid() || checkLocation.getBlock().getType().isSolid())
        {
            BlockState block = location.getBlock().getState();
            BlockState block1Up = checkLocation.getBlock().getState();
            // signpost OR plates ...
            if ((!block.getType().isSolid() || BlockUtil.isNonObstructingSolidBlock(block.getType()))
            && (!block1Up.getType().isSolid() || BlockUtil.isNonObstructingSolidBlock(block1Up.getType())))
            {
                break;
            }
            if (!block1Up.getType().isSolid()) // block on top is non Solid
            {
                BlockState block2Up = checkLocation.getBlock().getRelative(BlockFace.UP).getState();
                // If block & block2Up are Steps in the right direction add 0.5 to y and exit
                if ((block.getData() instanceof Step || block.getData() instanceof  WoodenStep)
                && (block2Up.getData() instanceof Step || block2Up.getData() instanceof WoodenStep))
                {
                    if (!isInvertedStep(block.getData()) && isInvertedStep(block2Up.getData()))
                    {
                        location.add(0,0.5,0);
                        break;
                    }
                }
            }
            location.add(0, 1, 0);
            checkLocation.add(0, 1, 0);
        }
        if (!this.isFlying())
        {
            checkLocation = location.clone();
            while (checkLocation.add(0, -1, 0).getBlock().getType() == Material.AIR)
            {
                location.add(0, -1, 0);
            }
        }
        checkLocation = location.clone().add(0, -1, 0);
        Block blockBelow = checkLocation.getBlock();
        if (blockBelow.getType() == Material.STATIONARY_LAVA || blockBelow.getType() == Material.LAVA)
        {
            location = location.getWorld().getHighestBlockAt(location).getLocation().add(0, 1, 0); // If would fall in lava tp on highest position.
            // If there is still lava then you shall burn!
        }
        if (blockBelow.getType().equals(Material.FENCE)
         || blockBelow.getType().equals(Material.NETHER_FENCE))
        {
            location.add(0, 0.5, 0);
        }
        if (blockBelow.getType().equals(Material.STEP) || blockBelow.getType().equals(Material.WOOD_STEP))
        {
            if (!isInvertedStep(blockBelow.getState().getData()))
            {
                location.setY(location.getBlockY() - 0.5);
            }
        }
        if (keepDirection)
        {
            final Location loc = this.getLocation();
            location.setPitch(loc.getPitch());
            location.setYaw(loc.getYaw());
        }
        this.teleport(location, cause);
    }

    public boolean isPasswordSet()
    {
        return this.entity.getPasswd().length > 0;
    }

    public void logout()
    {
        this.loggedInState = false;
    }

    public boolean isLoggedIn()
    {
        return this.loggedInState;
    }

    public void setPermission(String permission, boolean b)
    {
        PluginManager pm = Bukkit.getServer().getPluginManager();
        Permission perm;
        if (b)
        {
            perm = pm.getPermission(this.getName());
        }
        else
        {
            perm = pm.getPermission("!" + this.getName());
        }
        perm.getChildren().put(permission, b);
        this.recalculatePermissions();
    }

    public void setPermission(Map<String, Boolean> permissions)
    {
        this.setPermission(permissions, this.getPlayer());
    }

    /**
     * Use this method to assign permissions to a user while logging in
     *
     * @param permissions a map of permissions
     * @param player the player
     */
    public void setPermission(Map<String, Boolean> permissions, Player player)
    {
        String posName = this.getName();
        String negName = "-" + this.getName();
        PluginManager pm = Bukkit.getServer().getPluginManager();
        Permission posPerm = pm.getPermission(posName);
        Permission negPerm = pm.getPermission(negName);
        Map<String, Boolean> positive;
        Map<String, Boolean> negative;
        if (posPerm == null)
        {
            pm.addPermission(posPerm = new Permission(posName, PermissionDefault.FALSE, new HashMap<String, Boolean>()));
            positive = posPerm.getChildren();
        }
        else
        {
            positive = posPerm.getChildren();
        }
        if (negPerm == null)
        {
            pm.addPermission(negPerm = new Permission(negName, PermissionDefault.FALSE, new HashMap<String, Boolean>()));
            negative = negPerm.getChildren();
        }
        else
        {
            negative = negPerm.getChildren();
        }
        positive.clear();
        negative.clear();
        for (String perm : permissions.keySet())
        {
            if (perm.endsWith("*") && (perm.startsWith("-cubeengine.") || perm.startsWith("cubeengine.")))
            {
                continue;
            }
            if (permissions.get(perm))
            {
                positive.put(perm, true);
            }
            else
            {
                negative.put(perm, false);
            }
        }
        PermissionAttachment attachment = null;
        if (player.getEffectivePermissions() != null)
        {
            for (PermissionAttachmentInfo attachmentInfo : player.getEffectivePermissions())
            {
                if (attachmentInfo.getAttachment() != null && attachmentInfo.getAttachment().getPlugin() != null && attachmentInfo.getAttachment().getPlugin() instanceof BukkitCore)
                {
                    attachment = attachmentInfo.getAttachment();
                    break;
                }
            }
        }
        if (attachment == null)
        {
            attachment = player.addAttachment((Plugin)this.core);
            attachment.setPermission(posPerm, true);
            attachment.setPermission(negPerm, true);
        }
        player.recalculatePermissions();
    }

    public boolean isInvulnerable()
    {
        return BukkitUtils.isInvulnerable(this);
    }

    public void setInvulnerable(boolean state)
    {
        BukkitUtils.setInvulnerable(this, state);
    }

    public Long getWorldId()
    {
        try
        {
            return this.getCore().getWorldManager().getWorldId(this.getWorld());
        }
        catch (IllegalArgumentException ex)
        {
            return null;
        }
    }

    /**
     * Returns all entities in line of sight of this player
     *
     * @param distance the max distance
     * @return a set of all entities in line of sight OR null if not online
     */
    public TreeSet<Entity> getTargets(int distance)
    {
        if (this.getOfflinePlayer().isOnline())
        {
            final Location blockLoc = new Location(null, 0, 0, 0);
            final Location entityLoc = new Location(null, 0, 0, 0);
            final Location playerLoc = this.getLocation();
            Comparator<Entity> compare = new Comparator<Entity>()
            {
                final Location l1 = new Location(null, 0, 0, 0);
                final Location l2 = new Location(null, 0, 0, 0);

                @Override
                public int compare(Entity o1, Entity o2)
                {
                    o1.getLocation(l1);
                    o2.getLocation(l2);
                    return (int)(l1.distanceSquared(playerLoc) - l2.distanceSquared(playerLoc));
                }
            };
            BlockIterator iterator = new BlockIterator(
                    this.getPlayer().getWorld(),
                    this.getLocation().toVector(),
                    this.getEyeLocation().getDirection(),
                    0, distance);
            TreeSet<Entity> targets = new TreeSet<>(compare);
            Collection<Entity> list = this.getNearbyEntities(distance, distance, distance);
            double detectDistance = 1;
            while (iterator.hasNext())
            {
                Block block = iterator.next();
                detectDistance += 0.015;
                block.getLocation(blockLoc).add(0.5, 0.5, 0.5);
                for (Entity entity : list)
                {
                    if (entity.getLocation(entityLoc).distanceSquared(blockLoc) < ((entity instanceof Spider) ? detectDistance + 0.5 : detectDistance))
                    {
                        targets.add(entity);
                    }
                }
            }
            return targets;
        }
        return null;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        else if (o instanceof OfflinePlayer)
        {
            return this.getOfflinePlayer().equals(o);
        }
        else if (o instanceof CommandSender)
        {
            return ((CommandSender)o).getName().equals(this.getName());
        }
        else if (o instanceof org.bukkit.command.CommandSender)
        {
            return ((org.bukkit.command.CommandSender)o).getName().equals(this.getName());
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        return this.getOfflinePlayer().hashCode();
    }

    private InetSocketAddress address = null;
    public void refreshIP()
    {
        address = this.getPlayer().getAddress();
    }

    @Override
    public InetSocketAddress getAddress()
    {
        if (this.isOnline())
        {
            return super.getAddress();
        }
        return this.address;
    }

    public void banIp(CommandSender source, String reason)
    {
        this.banIp(source, reason, null);
    }

    public void banIp(CommandSender source, String reason, Date expire)
    {
        this.banIp(source, reason, new Date(System.currentTimeMillis()), expire);
    }

    public void banIp(CommandSender source, String reason, Date created, Date expire)
    {
        this.getCore().getBanManager().addBan(new IpBan(this.getAddress().getAddress(), source.getName(), reason, created, expire));
    }

    public void ban(CommandSender source, String reason)
    {
        this.ban(source, reason, null);
    }

    public void ban(CommandSender source, String reason, Date expire)
    {
        this.ban(source, reason, new Date(System.currentTimeMillis()), expire);
    }

    public void ban(CommandSender source, String reason, Date created, Date expire)
    {
        this.getCore().getBanManager().addBan(new UserBan(this.getName(), source.getName(), reason, created, expire));
    }

    public UserEntity getEntity()
    {
        return entity;
    }
}
