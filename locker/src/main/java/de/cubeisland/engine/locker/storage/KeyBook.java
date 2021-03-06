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

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.locker.Locker;

public class KeyBook
{
    public static final String TITLE = ChatFormat.parseFormats("&r&6KeyBook &8#");
    public final ItemStack item;
    public final User currentHolder;
    private Locker module;
    public final long lockID;
    private final String keyBookName;

    private KeyBook(ItemStack item, User currentHolder, Locker module)
    {
        this.item = item;
        this.currentHolder = currentHolder;
        this.module = module;
        keyBookName = item.getItemMeta().getDisplayName();
        lockID = Long.valueOf(keyBookName.substring(keyBookName.indexOf('#')+1, keyBookName.length()));
    }

    public static KeyBook getKeyBook(ItemStack item, User currentHolder, Locker module)
    {
        if (item.getType() == Material.ENCHANTED_BOOK &&
            item.getItemMeta().hasDisplayName() &&
            item.getItemMeta().getDisplayName().contains(KeyBook.TITLE))
        {
            try
            {
                return new KeyBook(item, currentHolder, module);
            }
            catch (NumberFormatException|IndexOutOfBoundsException ignore)
            {}
        }
        return null;
    }

    public boolean check(Lock lock, Location effectLocation)
    {
        if (lock.getId().equals(lockID)) // Id matches ?
        {
            // Validate book
            if (this.isValidFor(lock))
            {
                if (effectLocation != null) currentHolder.sendTranslated("&aAs you approach with your KeyBook the magic lock disappears!");
                currentHolder.playSound(effectLocation, Sound.PISTON_EXTEND, 1, 2);
                currentHolder.playSound(effectLocation, Sound.PISTON_EXTEND, 1, (float)1.5);
                if (effectLocation != null) lock.notifyKeyUsage(currentHolder);
                return true;
            }
            else
            {
                currentHolder.sendTranslated("&cYou try to open the container with your KeyBook\n" +
                                        "but forcefully get pushed away!");
                this.invalidate();
                currentHolder.playSound(effectLocation, Sound.GHAST_SCREAM, 1, 1);
                final Vector userDirection = currentHolder.getLocation().getDirection();
                currentHolder.damage(1);
                currentHolder.setVelocity(userDirection.multiply(-3));
                return false;
            }
        }
        else
        {
            currentHolder.sendTranslated("&eYou try to open the container with your KeyBook but nothing happens!");
            currentHolder.playSound(effectLocation, Sound.BLAZE_HIT, 1, 1);
            currentHolder.playSound(effectLocation, Sound.BLAZE_HIT, 1, (float)0.8);
            return false;
        }
    }

    public void invalidate()
    {
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatFormat.parseFormats("&4Broken KeyBook"));
        itemMeta.setLore(Arrays.asList(ChatFormat
               .parseFormats(currentHolder.translate("&eThis KeyBook")), ChatFormat
               .parseFormats(currentHolder.translate("&elooks old and")), ChatFormat
               .parseFormats(currentHolder.translate("&eused up. It")), ChatFormat
               .parseFormats(currentHolder.translate("&ewont let you")), ChatFormat
               .parseFormats(currentHolder.translate("&eopen any containers!"))));
        item.setItemMeta(itemMeta);
        item.setType(Material.PAPER);
        currentHolder.updateInventory();
    }

    public boolean isValidFor(Lock lock)
    {
        boolean b = keyBookName.startsWith(lock.getColorPass());
        if (!b)
        {
            this.module.getLog().debug("Invalid KeyBook detected! {}|{}", lock.getColorPass(), keyBookName);
        }
        return b;
    }
}
