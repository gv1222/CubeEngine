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
package de.cubeisland.engine.basics.command.moderation;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.basics.Basics;
import de.cubeisland.engine.basics.BasicsAttachment;
import de.cubeisland.engine.basics.BasicsPerm;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.InventoryGuardFactory;

/**
 * Contains commands that allow to modify an inventory.
 * <p>/invsee
 * <p>/clearinventory
 * <p>/stash
 */
public class InventoryCommands
{
    private Basics basics;

    public InventoryCommands(Basics basics)
    {
        this.basics = basics;
    }

    @Command(desc = "Allows you to see into the inventory of someone else.",
            flags = {@Flag(longName = "force", name = "f"),
                     @Flag(longName = "quiet", name = "q"),
                     @Flag(longName = "ender", name = "e")},
            usage = "<player>", min = 1, max = 1)
    public void invsee(ParameterizedContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            User user = context.getUser(0);
            if (user == null)
            {
                context.sendTranslated("&cUser &2%s &cnot found!", context.getString(0));
                return;
            }
            boolean allowModify = false;
            Inventory inv;
            if (context.hasFlag("e"))
            {
                if (BasicsPerm.COMMAND_INVSEE_ENDERCHEST.isAuthorized(sender))
                {
                    inv = user.getEnderChest();
                }
                else
                {
                    context.sendTranslated("&cYou are not allowed to look into enderchests!");
                    return;
                }
            }
            else
            {
                inv = user.getInventory();
            }
            if (BasicsPerm.COMMAND_INVSEE_MODIFY.isAuthorized(sender))
            {
                allowModify = true;
                if (!(context.hasFlag("f") && BasicsPerm.COMMAND_INVSEE_MODIFY_FORCE.isAuthorized(sender))
                    && BasicsPerm.COMMAND_INVSEE_MODIFY_PREVENT.isAuthorized(user))
                {
                    allowModify = false;
                }
            }
            if (BasicsPerm.COMMAND_INVSEE_NOTIFY.isAuthorized(user))
            {
                if (!(context.hasFlag("q") && BasicsPerm.COMMAND_INVSEE_QUIET.isAuthorized(context.getSender())))
                {
                    user.sendTranslated("&2%s&e is looking into your inventory.", sender.getName());
                }
            }
            InventoryGuardFactory guard = InventoryGuardFactory.prepareInventory(inv, sender);
            if (!allowModify)
            {
                guard.blockPutInAll().blockTakeOutAll();
            }
            guard.submitInventory(this.basics, true);
            return;
        }
        context.sendTranslated("&cThis command can only be used by a player!");
    }

    @Command(desc = "Stashes or unstashes your inventory to reuse later")
    public void stash(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            ItemStack[] stashedInv = sender.get(BasicsAttachment.class).getStashedInventory();
            ItemStack[] stashedArmor = sender.get(BasicsAttachment.class).getStashedArmor();
            ItemStack[] invToStash = sender.getInventory().getContents().clone();
            ItemStack[] armorToStash = sender.getInventory().getArmorContents().clone();
            if (stashedInv != null)
            {
                sender.getInventory().setContents(stashedInv);
            }
            else
            {
                sender.getInventory().clear();
            }

            sender.get(BasicsAttachment.class).setStashedInventory(invToStash);
            if (stashedArmor != null)
            {
                sender.getInventory().setBoots(stashedArmor[0]);
                sender.getInventory().setLeggings(stashedArmor[1]);
                sender.getInventory().setChestplate(stashedArmor[2]);
                sender.getInventory().setHelmet(stashedArmor[3]);
            }
            else
            {
                sender.getInventory().setBoots(null);
                sender.getInventory().setLeggings(null);
                sender.getInventory().setChestplate(null);
                sender.getInventory().setHelmet(null);
            }
            sender.get(BasicsAttachment.class).setStashedArmor(armorToStash);
            sender.sendTranslated("&aSwapped stashed Inventory!");
            return;
        }
        context.sendTranslated("&cYeah you better put it away!");
    }

    @Command(names = {"clearinventory", "ci", "clear"},
            desc = "Clears the inventory", usage = "[player]",
            flags = {@Flag(longName = "removeArmor", name = "ra")
            ,@Flag(longName = "quiet", name = "q")}, max = 1)
    @SuppressWarnings("deprecation")
    public void clearinventory(ParameterizedContext context)
    {
        CommandSender sender = context.getSender();
        final User target;
        if (context.hasArgs())
        {
            target = context.getArg(0, User.class);
            if (target == null)
            {
                sender.sendTranslated("&cThe specified user was not found!");
                return;
            }
        }
        else if (sender instanceof User)
        {
            target = (User)sender;
        }
        else
        {
            sender.sendTranslated("&cThat awkward moment when you realize you do not have an inventory!");
            return;
        }
        if (sender != target && !BasicsPerm.COMMAND_CLEARINVENTORY_OTHER.isAuthorized(sender))
        {
            context.sendTranslated("&cYou are not allowed to clear the inventory of other users!");
            return;
        }
        if (target != sender && BasicsPerm.COMMAND_CLEARINVENTORY_PREVENT.isAuthorized(target)) // other has prevent
        {
            if (!(context.hasFlag("f") && BasicsPerm.COMMAND_CLEARINVENTORY_FORCE.isAuthorized(sender))) // is not forced?
            {
                context.sendTranslated("&cYou are not allowed to clear the inventory of &2%s", target.getName());
                return;
            }
        }
        target.getInventory().clear();
        if (context.hasFlag("ra"))
        {
            target.getInventory().setBoots(null);
            target.getInventory().setLeggings(null);
            target.getInventory().setChestplate(null);
            target.getInventory().setHelmet(null);
        }
        target.updateInventory();
        if (sender == target)
        {
            sender.sendTranslated("&aYour inventory has been cleared!");
        }
        else
        {
            if (BasicsPerm.COMMAND_CLEARINVENTORY_NOTIFY.isAuthorized(target)) // notify
            {
                if (!(BasicsPerm.COMMAND_CLEARINVENTORY_QUIET.isAuthorized(sender) && context.hasFlag("q"))) // quiet
                {
                    target.sendTranslated("&eYour inventory has been cleared by &6%s&e!", sender.getName());
                }
            }
            sender.sendTranslated("&aThe inventory of &6%s&a has been cleared!", target.getName());
        }
    }
}
