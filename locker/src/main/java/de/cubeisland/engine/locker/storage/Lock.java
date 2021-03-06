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
package de.cubeisland.engine.locker.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Door;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.InventoryGuardFactory;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.locker.LockerAttachment;
import de.cubeisland.engine.locker.LockerPerm;
import org.jooq.Result;

import static de.cubeisland.engine.locker.storage.AccessListModel.*;
import static de.cubeisland.engine.locker.storage.LockType.PUBLIC;
import static de.cubeisland.engine.locker.storage.TableAccessList.TABLE_ACCESS_LIST;

public class Lock
{
    private LockManager manager;
    protected final LockModel model;
    protected final ArrayList<Location> locations = new ArrayList<>();

    private Integer taskId = null; // for autoclosing doors

    /**
     * EntityLock
     *
     * @param manager
     * @param model
     */
    public Lock(LockManager manager, LockModel model)
    {
        this.manager = manager;
        this.model = model;
        this.checkLockType();
    }

    /**
     * BlockLock
     *
     * @param manager
     * @param model
     * @param lockLocs
     */
    public Lock(LockManager manager, LockModel model, Result<LockLocationModel> lockLocs)
    {
        this(manager, model);
        for (LockLocationModel lockLoc : lockLocs)
        {
            this.locations.add(this.getLocation(lockLoc));
        }
        this.isValidType = false;
    }

    public Lock(LockManager manager, LockModel model, List<Location> locations)
    {
        this(manager, model);
        this.locations.addAll(locations);
        this.isValidType = false;
    }

    public void invalidateKeyBooks()
    {
        this.model.createPassword(this.manager, null);
        this.model.update();
    }


    public void showCreatedMessage(User user)
    {
        switch (this.getLockType())
        {
        case PRIVATE:
            user.sendTranslated("&aPrivate Lock created!");
            break;
        case PUBLIC:
            user.sendTranslated("&aPublic Lock created!");
            break;
        case GUARDED:
            user.sendTranslated("&aGuarded Lock created!");
            break;
        case DONATION:
            user.sendTranslated("&aDonation Lock created!");
            break;
        case FREE:
            user.sendTranslated("&aFree Lock created!");
            break;
        }
    }

    public boolean handleAccess(User user, Location soundLocation, Cancellable event)
    {
        if (this.isOwner(user)) return true;
        Boolean keyBookUsed = this.checkForKeyBook(user, soundLocation);
        if (keyBookUsed != null && !keyBookUsed)
        {
            event.setCancelled(true);
            return false;
        }
        return keyBookUsed != null || this.checkForUnlocked(user) || LockerPerm.ACCESS_OTHER.isAuthorized(user);
    }

    public boolean checkForUnlocked(User user)
    {
        LockerAttachment lockerAttachment = user.get(LockerAttachment.class);
        return lockerAttachment != null && lockerAttachment.hasUnlocked(this);
    }

    public void attemptCreatingKeyBook(User user, Boolean third)
    {
        if (this.getLockType() == PUBLIC) return; // ignore
        if (!this.manager.module.getConfig().allowKeyBooks)
        {
            user.sendTranslated("&aKeyBooks are not enabled!");
            return;
        }
        if (third)
        {
            if (user.getItemInHand().getType() == Material.BOOK)
            {
                int amount = user.getItemInHand().getAmount() -1;
                ItemStack itemStack = new ItemStack(Material.ENCHANTED_BOOK, 1);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(this.getColorPass() + KeyBook.TITLE + this.getId());
                itemMeta.setLore(Arrays.asList(user.translate(ChatFormat.parseFormats("&eThis book can")), user
                    .translate(ChatFormat.parseFormats("&eunlock a magically")), user
                                                   .translate(ChatFormat.parseFormats("&elocked protection"))));
                itemStack.setItemMeta(itemMeta);
                user.setItemInHand(itemStack);
                HashMap<Integer, ItemStack> full = user.getInventory().addItem(new ItemStack(Material.BOOK, amount));
                for (ItemStack stack : full.values())
                {
                    Location location = user.getLocation();
                    location.getWorld().dropItem(location, stack);
                }
                user.updateInventory();
            }
            else
            {
                user.sendTranslated("&cCould not create KeyBook! You need to hold a book in your hand in order to do this!");
            }
        }
    }

    /**
     * Sets the AccessLevel for given user
     *
     * @param modifyUser the user set set the accesslevel for
     * @param add whether to add to remove the access
     * @param level the accesslevel
     * @return false when updating or not deleting <p>true when inserting or deleting
     */
    public boolean setAccess(User modifyUser, boolean add, short level)
    {
        AccessListModel model = this.getAccess(modifyUser);
        if (add)
        {
            if (model == null)
            {
                model = this.manager.dsl.newRecord(TABLE_ACCESS_LIST).newAccess(this.model, modifyUser);
                model.setLevel(level);
                model.insert();
            }
            else
            {
                model.setLevel(level);
                model.update();
                return false;
            }
        }
        else // remove lock
        {
            if (model == null) return false;
            model.delete();
        }
        return true;
    }

    /**
     * Sets multiple acceslevels
     *
     * @param user the user modifying
     * @param usersString
     */
    public void modifyLock(User user, String usersString)
    {
        if (this.isOwner(user) || this.hasAdmin(user) || LockerPerm.CMD_MODIFY_OTHER.isAuthorized(user))
        {
            if (!this.isPublic())
            {
                String[] explode = StringUtils.explode(",", usersString);
                for (String name : explode)
                {
                    boolean add = true;
                    boolean admin = false;
                    if (name.startsWith("@"))
                    {
                        name = name.substring(1);
                        admin = true;
                    }
                    if (name.startsWith("-"))
                    {
                        name = name.substring(1);
                        add = false;
                    }
                    User modifyUser = this.manager.um.getUser(name, false);
                    if (modifyUser == null) throw new IllegalArgumentException(); // This is prevented by checking first in the cmd execution
                    short accessType = ACCESS_FULL;
                    if (add && admin)
                    {
                        accessType = ACCESS_ALL; // with AdminAccess
                    }
                    if (this.setAccess(modifyUser, add, accessType))
                    {
                        if (add)
                        {
                            if (admin)
                            {
                                user.sendTranslated("&aGranted &2%s&a admin access to this protection!", modifyUser.getName());
                            }
                            else
                            {
                                user.sendTranslated("&aGranted &2%s&a access to this protection!", modifyUser.getName());
                            }
                        }
                        else
                        {
                            user.sendTranslated("&aRemoved &2%s's&a access to this protection!", modifyUser.getName());
                        }
                    }
                    else
                    {
                        if (add)
                        {
                            if (admin)
                            {
                                user.sendTranslated("&aUdated &2%s's&a access to admin access!", modifyUser.getName());
                            }
                            else
                            {
                                user.sendTranslated("&aUdated &2%s's&a access to normal access!", modifyUser.getName());
                            }
                        }
                        else
                        {
                            user.sendTranslated("&2%s&a had no access to this protection!", modifyUser.getName());
                        }
                    }
                }
                return;
            }
            user.sendTranslated("&eThis protection is public and so accessible to everyone");
            return;
        }
        user.sendTranslated("&cYou are not allowed to modify the access-list of this protection!");
    }

    /**
     * Returns true if the chest could open
     * <p>false if the chest cannot be opened with the KeyBook
     * <p>null if the user has no KeyBook in hand
     *
     * @param user
     * @param effectLocation
     * @return
     */
    public Boolean checkForKeyBook(User user, Location effectLocation)
    {
        KeyBook keyBook = KeyBook.getKeyBook(user.getItemInHand(), user, this.manager.module);
        if (keyBook != null)
        {
            return keyBook.check(this, effectLocation);
        }
        return null;
    }

    public boolean checkPass(String pass)
    {
        if (!this.hasPass()) return false;

        synchronized (this.manager.messageDigest)
        {
            this.manager.messageDigest.reset();
            return Arrays.equals(this.manager.messageDigest.digest(pass.getBytes()), this.model.getPassword());
        }
    }

    private Location getLocation(LockLocationModel model)
    {
        return new Location(this.manager.wm.getWorld(model.getWorldId().longValue()), model.getX(), model.getY(), model.getZ());
    }

    public boolean isBlockLock()
    {
        return !this.locations.isEmpty();
    }

    public boolean isSingleBlockLock()
    {
        return this.locations.size() == 1;
    }

    public Location getFirstLocation()
    {
        return this.locations.get(0);
    }

    public ArrayList<Location> getLocations()
    {
        return this.locations;
    }

    public void handleBlockDoorUse(Cancellable event, User user, Location clickedDoor)
    {
        if (this.getLockType() == PUBLIC)
        {
            this.doorUse(user, clickedDoor);
            return; // Allow everything
        }
        if (this.handleAccess(user, clickedDoor, event))
        {
            this.doorUse(user, clickedDoor);
            return;
        }
        if (event.isCancelled()) return;
        if (this.model.getOwnerId().equals(user.getEntity().getKey())) return; // Its the owner
        switch (this.getLockType())
        {
            case PRIVATE: // block changes
                break;
            case GUARDED:
            case DONATION:
            case FREE:
            default: // Not Allowed for doors
                throw new IllegalStateException();
        }
        AccessListModel access = this.getAccess(user);
        if (access == null || !(access.canIn() && access.canOut()) || LockerPerm.ACCESS_OTHER.isAuthorized(user)) // No access Or not full access
        {
            event.setCancelled(true);
            if (LockerPerm.SHOW_OWNER.isAuthorized(user))
            {
                user.sendTranslated("&cA magical lock from &2%s&c prevents you from using this door!", this.getOwner().getName());
            }
            else
            {
                user.sendTranslated("&cA magical lock prevents you from using this door!");
            }
            return;
        } // else has access
        this.doorUse(user, clickedDoor);
    }

    private AccessListModel getAccess(User user)
    {
        AccessListModel model = this.manager.dsl.selectFrom(TABLE_ACCESS_LIST).
            where(TABLE_ACCESS_LIST.LOCK_ID.eq(this.model.getId()),
                  TABLE_ACCESS_LIST.USER_ID.eq(user.getEntity().getKey())).fetchOne();
        if (model == null)
        {
            model = this.manager.dsl.selectFrom(TABLE_ACCESS_LIST).
                where(TABLE_ACCESS_LIST.USER_ID.eq(user.getEntity().getKey()),
                      TABLE_ACCESS_LIST.OWNER_ID.eq(this.getOwner().getEntity().getKey())).fetchOne();
        }
        return model;
    }

    public void handleInventoryOpen(Cancellable event, Inventory protectedInventory, Location soundLocation, User user)
    {
        if (soundLocation != null && LockerPerm.SHOW_OWNER.isAuthorized(user))
        {
            user.sendTranslated("&eThis inventory is protected by &2%s", this.getOwner().getName());
        }
        if (this.handleAccess(user, soundLocation, event) || event.isCancelled()) return;
        boolean in;
        boolean out;
        switch (this.getLockType())
        {
            default: throw new IllegalStateException();
            case PUBLIC: return; // Allow everything
            case PRIVATE: // block changes
            case GUARDED:
                in = false;
                out = false;
                break;
            case DONATION:
                in = true;
                out = false;
                break;
            case FREE:
                in = false;
                out = true;
        }
        AccessListModel access = this.getAccess(user);
        if (access == null && this.getLockType() == LockType.PRIVATE && !LockerPerm.ACCESS_OTHER.isAuthorized(user))
        {
            event.setCancelled(true); // private & no access
            if (LockerPerm.SHOW_OWNER.isAuthorized(user))
            {
                user.sendTranslated("&cA magical lock from &2%s&c prevents you from accessing this inventory!", this.getOwner().getName());
            }
            else
            {
                user.sendTranslated("&cA magical lock prevents you from accessing this inventory!");
            }
        }
        else // Has access access -> new InventoryGuard
        {
            if (access != null)
            {
                in = in || access.canIn();
                out = out || access.canOut();
            }
            this.notifyUsage(user);
            if ((in && out) || LockerPerm.ACCESS_OTHER.isAuthorized(user)) return; // Has full access
            if (protectedInventory == null) return; // Just checking else do lock
            InventoryGuardFactory inventoryGuardFactory = InventoryGuardFactory.prepareInventory(protectedInventory, user);
            if (!in)
            {
                inventoryGuardFactory.blockPutInAll();
            }
            if (!out)
            {
                inventoryGuardFactory.blockTakeOutAll();
            }
            inventoryGuardFactory.submitInventory(this.manager.module, false);

            this.notifyUsage(user);
        }
    }

    public void handleEntityInteract(Cancellable event, User user)
    {
        if (LockerPerm.SHOW_OWNER.isAuthorized(user))
        {
            user.sendTranslated("&eThis entity is protected by &2%s", this.getOwner().getName());
        }
        if (this.getLockType() == PUBLIC) return;
        if (this.handleAccess(user, null, event))
        {
            this.notifyUsage(user);
            return;
        }
        if (event.isCancelled())
        {
            return;
        }
        AccessListModel access = this.getAccess(user);
        if (access == null && this.getLockType() == LockType.PRIVATE)
        {
            event.setCancelled(true); // private & no access
            if (LockerPerm.SHOW_OWNER.isAuthorized(user))
            {
                user.sendTranslated("&cMagic from &2%s&c repelled your attempts to reach this entity!", this.getOwner().getName());
            }
            else
            {
                user.sendTranslated("&cMagic repelled your attempts to reach this entity!");
            }
        }
        this.notifyUsage(user);
    }

    private void checkLockType()
    {
        if (this.getLockType().supportedTypes.contains(this.getProtectedType())) return;
        throw new IllegalStateException("LockType is not supported for " + this.getProtectedType().name() + ":" + this.getLockType().name());
    }

    public ProtectedType getProtectedType()
    {
        return ProtectedType.forByte(this.model.getType());
    }

    public LockType getLockType()
    {
        return LockType.forByte(this.model.getLockType());
    }

    public void handleBlockBreak(BlockBreakEvent event, User user)
    {
        if (this.model.getOwnerId().equals(user.getEntity().getKey()) || LockerPerm.BREAK_OTHER.isAuthorized(user))
        {
            this.delete(user);
            return;
        }
        event.setCancelled(true);
        user.sendTranslated("&cMagic prevents you from breaking this protection!");
    }

    public boolean handleEntityDamage(Cancellable event, User user)
    {
        if (this.model.getOwnerId().equals(user.getEntity().getKey()) || LockerPerm.BREAK_OTHER.isAuthorized(user))
        {
            user.sendTranslated("&eThe magic surrounding this entity quivers as you hit it!");
            return true;
        }
        event.setCancelled(true); // private & no access
        user.sendTranslated("&cMagic repelled your attempts to hit this entity!");
        return false;
    }

    public void handleEntityDeletion(User user)
    {
        this.delete(user);
    }

    /**
     * Deletes a protection and informs the given user
     *
     * @param user
     */
    public void delete(User user)
    {
        this.manager.removeLock(this, user, true);
    }

    public boolean isOwner(User user)
    {
        return this.model.getOwnerId().equals(user.getEntity().getKey());
    }

    public boolean hasAdmin(User user)
    {
        AccessListModel access = this.getAccess(user);
        return access != null && (access.getLevel() & ACCESS_ADMIN) == ACCESS_ADMIN;
    }

    public String getColorPass()
    {
        return this.model.getColorPass();
    }

    public Long getId()
    {
        return this.model.getId().longValue();
    }

    public boolean hasPass()
    {
        return this.model.getPassword().length > 4;
    }

    private Map<String, Long> lastKeyNotify;

    public void notifyKeyUsage(User user)
    {
        if (lastKeyNotify == null)
        {
            this.lastKeyNotify = new HashMap<>();
        }
        User owner = this.manager.um.getUser(this.model.getOwnerId().longValue());
        Long last = this.lastKeyNotify.get(owner.getName());
        if (last == null || TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - last) > 60) // 60 sec config ?
        {
            this.lastKeyNotify.put(owner.getName(), System.currentTimeMillis());
            owner.sendTranslated("&2%s&e used a KeyBook to access one of your protections!", user.getName());
        }
    }

    private Map<String, Long> lastNotify;

    public void notifyUsage(User user)
    {
        if (LockerPerm.PREVENT_NOTIFY.isAuthorized(user)) return;
        if (this.hasFlag(ProtectionFlag.NOTIFY_ACCESS))
        {
            if (user.equals(this.getOwner()))
            {
                return;
            }
            if (lastNotify == null)
            {
                this.lastNotify = new HashMap<>();
            }
            User owner = this.manager.um.getUser(this.model.getOwnerId().longValue());
            Long last = this.lastNotify.get(owner.getName());
            if (last == null || TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - last) > 60) // 60 sec config ?
            {
                this.lastNotify.put(owner.getName(), System.currentTimeMillis());
                if (this.isBlockLock())
                {
                    owner.sendTranslated("&2%s&e accessed one your protection with the id &6%d!", user.getName(), this.getId());
                    Location loc = this.getFirstLocation();
                    owner.sendTranslated("&ewhich is located at &6%d&e:&6%s&e:&6%s&e in &6%s&e!",
                         loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName());
                }
                else
                {
                    for (Entity entity : user.getWorld().getEntities())
                    {
                        if (entity.getUniqueId().equals(this.getEntityUID()))
                        {
                            owner.sendTranslated("&2%s&e accessed one of your protected entities!", user.getName());
                            Location loc = entity.getLocation();
                            owner.sendTranslated("&ewhich is located at &6%d&e:&6%s&e:&6%s&e in &6%s&e!",
                                                 loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName());
                            return;
                        }
                    }
                    owner.sendTranslated("&2%s&e accessed one of your protected entities somewhere!", user.getName());
                }
            }
        }
    }

    public User getOwner()
    {
        return this.manager.module.getCore().getUserManager().getUser(this.model.getOwnerId().longValue());
    }

    public boolean isPublic()
    {
        return this.getLockType() == PUBLIC;
    }

    public boolean hasFlag(ProtectionFlag flag)
    {
        return flag.flagValue == (this.model.getFlags() & flag.flagValue);
    }

    public void showInfo(User user)
    {
        if (this.isOwner(user) || this.hasAdmin(user) || LockerPerm.CMD_INFO_OTHER.isAuthorized(user))
        {
            user.sendMessage("");
            user.sendTranslated("&aProtection: &6#%d&a Type: &6%s&a by &6%s", this.getId(), this.getLockType().name(), this.getOwner().getName());
            user.sendTranslated("&aprotects &6%s&a since &6%s", this.getProtectedType().name(), this.model.getCreated().toString());
            user.sendTranslated("&alast access was &6%s", this.model.getLastAccess().toString());
            if (this.hasPass())
            {
                if (user.attachOrGet(LockerAttachment.class, this.manager.module).hasUnlocked(this))
                {
                    user.sendTranslated("&aHas a password and is currently &eunlocked");
                }
                else
                {
                    user.sendTranslated("&aHas a password and is currently &clocked");
                }
            }


            List<String> flags = new ArrayList<>();
            for (ProtectionFlag flag : ProtectionFlag.values())
            {
                if (this.hasFlag(flag))
                {
                    flags.add(flag.flagname);
                }
            }
            if (!flags.isEmpty())
            {
                user.sendTranslated("&aThe following flags are set:");
                String format = ChatFormat.parseFormats("  &7- &e%s");
                for (String flag : flags)
                {
                    user.sendMessage(String.format(format, flag));
                }
            }
            List<AccessListModel> accessors = this.getAccessors();
            if (!accessors.isEmpty())
            {
                user.sendTranslated("&aThe following users do have direct access to this protection");
                for (AccessListModel listModel : accessors)
                {
                    User accessor = this.manager.module.getCore().getUserManager().getUser(listModel.getUserId().longValue());
                    if ((listModel.getLevel() & ACCESS_ADMIN) == ACCESS_ADMIN)
                    {
                        user.sendMessage(String.format(ChatFormat.parseFormats("  &7- &2%s&6 [Admin]"), accessor.getName()));
                    }
                    else
                    {
                        user.sendMessage(String.format(ChatFormat.parseFormats("  &7- &2%s"), accessor.getName()));
                    }
                }
            }
            if (!this.locations.isEmpty())
            {
                user.sendTranslated("&aThis protections covers &6%d&a blocks!", this.locations.size());
            }
        }
        else
        {
            if (LockerPerm.CMD_INFO_SHOW_OWNER.isAuthorized(user))
            {
                user.sendTranslated("&aProtectionType: &6%s&a Owner: &2%s", this.getLockType().name(), this.getOwner().getName());
            }
            else
            {
                user.sendTranslated("&aProtectionType: &6%s", this.getLockType().name());
            }
            AccessListModel access = this.getAccess(user);
            if (this.hasPass())
            {
                if (user.attachOrGet(LockerAttachment.class, this.manager.module).hasUnlocked(this))
                {
                    user.sendTranslated("&aAs you memorize the pass-phrase the magic aura protecting this allows you to interact");
                }
                else
                {
                    user.sendTranslated("&aYou sense that the strong magic aura protecting this wont let you through without the right pass-phrase");
                }
            }
            else
            {
                user.sendTranslated("&aYou sense a strong magic aura protecting this");
            }
            if (access != null)
            {

                if (access.canIn() && access.canOut())
                {
                    if (this.getProtectedType() == ProtectedType.CONTAINER
                        || this.getProtectedType() == ProtectedType.ENTITY_CONTAINER
                        || this.getProtectedType() == ProtectedType.ENTITY_CONTAINER_LIVING)
                    {
                        user.sendTranslated("&abut is does not hinder you to put/take items in/out");
                    }
                    else
                    {
                        user.sendTranslated("&abut it lets you interact as if you were not there");
                    }
                }
            }
        }
        if (this.manager.module.getConfig().protectWhenOnlyOffline && this.getOwner().isOnline())
        {
            user.sendTranslated("&eThe protection is currently not active because its owner is online!");
        }
        if (this.manager.module.getConfig().protectWhenOnlyOnline && !this.getOwner().isOnline())
        {
            user.sendTranslated("&eThe protection is currently not active because its owner is offline!");
        }
    }

    public List<AccessListModel> getAccessors()
    {
        return this.manager.dsl.selectFrom(TABLE_ACCESS_LIST).
            where(TABLE_ACCESS_LIST.LOCK_ID.eq(this.model.getId())).fetch();
    }

    public void unlock(User user, Location soundLoc, String pass)
    {
        if (this.hasPass())
        {
            if (this.checkPass(pass))
            {
                user.sendTranslated("&aUpon hearing the right pass-phrase the magic surrounding the container gets thinner and lets you pass!");
                user.playSound(soundLoc, Sound.PISTON_EXTEND, 1, 2);
                user.playSound(soundLoc, Sound.PISTON_EXTEND, 1, (float)1.5);
                user.attachOrGet(LockerAttachment.class, this.manager.module).addUnlock(this);
            }
            else
            {
                user.sendTranslated("&eSudden pain makes you realize this was not the right pass-phrase!");
                user.damage(0);
            }
        }
        else
        {
            user.sendTranslated("&eYou try to open the container with a pass-phrase but nothing changes!");
        }
    }

    /**
     * If this lock protects a double-door this will open/close the second door too.
     * Also this will schedule auto-closing the door according to the configuration
     *
     * @param user
     * @param doorClicked
     */
    private void doorUse(User user, Location doorClicked)
    {
        Block block = doorClicked.getBlock();
        if (block.getType() == Material.IRON_DOOR_BLOCK && !this.manager.module.getConfig().openIronDoorWithClick)
        {
            user.sendTranslated("&eYou cannot open the heavy door!");
            return;
        }
        if (LockerPerm.SHOW_OWNER.isAuthorized(user))
        {
            user.sendTranslated("&eThis door is protected by &2%s", this.getOwner().getName());
        }
        if (block.getState().getData() instanceof Door)
        {
            Door door;
            if (((Door)block.getState().getData()).isTopHalf())
            {
                block = block.getRelative(BlockFace.DOWN);
                door = (Door)block.getState().getData();
            }
            else
            {
                door = (Door)block.getState().getData();
            }
            Sound sound;
            if (door.isOpen())
            {
                sound = Sound.DOOR_CLOSE;
            }
            else
            {
                sound = Sound.DOOR_OPEN;
            }
            Door door2 = null;
            Location loc2 = null;
            for (Location location : locations)
            {
                if (location.getBlockY() == block.getY() && !location.equals(block.getLocation(doorClicked)))
                {
                    door2 = (Door)location.getBlock().getState().getData();
                    loc2 = location;
                    break;
                }
            }
            if (door2 == null)
            {
                if (door.getItemType() == Material.IRON_DOOR_BLOCK)
                {
                    doorClicked.getWorld().playSound(doorClicked, sound, 1, 1);
                    door.setOpen(!door.isOpen());
                    block.setData(door.getData());
                }
                if (taskId != null) this.manager.module.getCore().getTaskManager().cancelTask(this.manager.module, taskId);
                if (sound == Sound.DOOR_OPEN) this.scheduleAutoClose(door, block.getState(), null, null);
            }
            else
            {
                boolean old = door.isOpen();
                door2.setOpen(!door.isOpen()); // Flip
                if (old != door2.isOpen())
                {

                    doorClicked.getWorld().playSound(loc2, sound, 1, 1);
                    loc2.getBlock().setData(door2.getData());
                    if (door.getItemType() == Material.IRON_DOOR_BLOCK)
                    {
                        doorClicked.getWorld().playSound(doorClicked, sound, 1, 1);
                        door.setOpen(door2.isOpen());
                        block.setData(door.getData());
                    }
                }
                if (taskId != null) this.manager.module.getCore().getTaskManager().cancelTask(this.manager.module, taskId);
                if (sound == Sound.DOOR_OPEN) this.scheduleAutoClose(door, block.getState(), door2, loc2.getBlock().getState());
            }
            this.notifyUsage(user);
        }
    }

    private void scheduleAutoClose(final Door door1, final BlockState state1, final Door door2, final BlockState state2)
    {
        if (this.hasFlag(ProtectionFlag.AUTOCLOSE))
        {
            if (!this.manager.module.getConfig().autoCloseEnable) return;
            taskId = this.manager.module.getCore().getTaskManager().runTaskDelayed(this.manager.module, new Runnable()
            {
                @Override
                public void run()
                {
                    door1.setOpen(false);
                    state1.setData(door1);
                    if (state1.update())
                    {
                        Location location = state1.getLocation();
                        location.getWorld().playSound(location, Sound.DOOR_CLOSE, 1, 1);
                    }
                    if (door2 != null)
                    {
                        door2.setOpen(false);
                        state2.setData(door2);
                        if (state2.update())
                        {
                            Location location = state2.getLocation();
                            location.getWorld().playSound(location, Sound.DOOR_CLOSE, 1, 1);
                        }
                    }
                }
            }, this.manager.module.getConfig().autoCloseSeconds * 20);
        }
    }

    /**
     * Always true for EntityLocks
     */
    private boolean isValidType = true;

    public boolean validateTypeAt(Location location)
    {
        if (ProtectedType.getProtectedType(location.getBlock().getType()) == this.getProtectedType())
        {
            this.isValidType = true;
        }
        else
        {
            this.manager.module.getLog().warn("ProtectedTypes do not match for Guard at {}" ,location.toString());
        }
        return this.isValidType;
    }

    public void setOwner(User owner)
    {
        this.model.setOwnerId(owner.getEntity().getKey());
        this.model.update();
    }

    public void setFlags(short flags)
    {
        this.model.setFlags(flags);
        this.model.update();
    }

    public short getFlags()
    {
        return this.model.getFlags();
    }

    public UUID getEntityUID()
    {
        return this.model.getUUID();
    }
}