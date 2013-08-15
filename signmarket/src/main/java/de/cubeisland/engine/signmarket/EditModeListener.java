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
package de.cubeisland.engine.signmarket;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandResult;
import de.cubeisland.engine.core.command.converstion.ConversationCommand;
import de.cubeisland.engine.core.command.converstion.ConversationContextFactory;
import de.cubeisland.engine.core.command.parameterized.CommandFlag;
import de.cubeisland.engine.core.command.parameterized.CommandParameter;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.user.User;

import gnu.trove.map.hash.TLongObjectHashMap;

public class EditModeListener extends ConversationCommand
{
    private final MarketSignFactory signFactory;
    private final SignMarketConfig config;

    public EditModeListener(Signmarket signmarket) {
        super(signmarket, new ConversationContextFactory());
        this.signFactory = signmarket.getMarketSignFactory();
        this.config = signmarket.getConfig();

        this.getContextFactory()
                .addFlag(new CommandFlag("exit", "exit"))
                .addFlag(new CommandFlag("copy", "copy"))
                .addFlag(new CommandFlag("buy", "buy"))
                .addFlag(new CommandFlag("sell","sell"))
                .addFlag(new CommandFlag("admin","admin"))
                .addFlag(new CommandFlag("user","user"))
                .addFlag(new CommandFlag("stock", "stock"))
                .addParameter(new CommandParameter("demand", Integer.class))
                .addParameter(new CommandParameter("owner", User.class))
                .addParameter(new CommandParameter("price", String.class))
                .addParameter(new CommandParameter("amount", Integer.class))
                .addParameter(new CommandParameter("item", ItemStack.class))
                .addParameter(new CommandParameter("setstock",Integer.class))
                .addParameter(new CommandParameter("size",Integer.class))
        ;
    }
//TODO itemblacklist?
    private TLongObjectHashMap<Location> currentSignLocation = new TLongObjectHashMap<>();
    private TLongObjectHashMap<MarketSign> previousMarketSign = new TLongObjectHashMap<>();

    // TODO CE-420 shift-click to edit multiple signs at the same time

    private boolean setEditingSign(User user, MarketSign marketSign)
    {
        Location previous = this.currentSignLocation.put(user.getId(), marketSign.getLocation());
        if (!marketSign.getLocation().equals(previous))
        {
            MarketSign previousSign = this.signFactory.getSignAt(previous);
            if (previousSign != null)
            {
                this.previousMarketSign.put(user.getId(), previousSign);
                previousSign.exitEditMode(user);
            }
            marketSign.enterEditMode();
            user.sendTranslated("&aChanged active sign!");
            return true;
        }
        marketSign.enterEditMode();
        return false;
    }

    @Override
    public void removeUser(User user)
    {
        super.removeUser(user);
        user.sendTranslated("&aEdit mode quit!");
    }

    public CommandResult run(CommandContext runContext) throws Exception
    {
        User user = (User)runContext.getSender();
        ParameterizedContext context = (ParameterizedContext) runContext;
        Location loc = this.currentSignLocation.get(user.getId());
        if (loc == null)
        {
            if (context.hasFlag("exit"))
            {
                this.removeUser(user);
                return null;
            }
            user.sendTranslated("&cPlease do select a sign to edit.");
            return null;
        }
        MarketSign marketSign = this.signFactory.getSignAt(loc);
        if (marketSign == null)
        {
            user.sendTranslated("&4No market-sign at position! This should not happen!");
            return null;
        }
        this.setEditingSign(user, marketSign);
        if (context.hasFlag("copy"))
        {
            MarketSign prevMarketSign = this.previousMarketSign.get(user.getId());
            if (prevMarketSign == null)
            {
                user.sendTranslated("&cNo market-sign at previous position.");
            }
            else
            {
                marketSign.copyValuesFrom(prevMarketSign);
            }
        }
        if (context.hasFlag("buy"))
        {
            if (marketSign.isAdminSign())
            {
                if (MarketSignPerm.SIGN_CREATE_ADMIN_BUY.isAuthorized(user))
                {
                    marketSign.setTypeBuy();
                }
                else
                {
                    context.sendTranslated("&cYou are not allowed to create admin-buy signs!");
                }
            }
            else
            {
                if (MarketSignPerm.SIGN_CREATE_USER_BUY.isAuthorized(user))
                {
                    marketSign.setTypeBuy();
                }
                else
                {
                    context.sendTranslated("&cYou are not allowed to create user-buy signs!");
                }
            }
        }
        if (context.hasFlag("sell"))
        {
            if (marketSign.isAdminSign())
            {
                if (MarketSignPerm.SIGN_CREATE_ADMIN_SELL.isAuthorized(user))
                {
                    marketSign.setTypeSell();
                }
                else
                {
                    context.sendTranslated("&cYou are not allowed to create admin-sell signs!");
                }
            }
            else
            {
                if (MarketSignPerm.SIGN_CREATE_USER_SELL.isAuthorized(user))
                {
                    marketSign.setTypeSell();
                }
                else
                {
                    context.sendTranslated("&cYou are not allowed to create user-sell signs!");
                }
            }
        }
        if (context.hasParam("demand"))
        {
            if (marketSign.isTypeBuy() == null)
            {
                marketSign.setTypeSell();
            }
            if (marketSign.isTypeBuy())
            {
                user.sendTranslated("&cBuy signs cannot have a demand!");
            }
            else if (marketSign.isAdminSign())
            {
                user.sendTranslated("&cAdmin signs cannot have a demand!");
            }
            else
            {
                Integer demand = context.getParam("demand",null);
                if (demand == -1)
                {
                    marketSign.setNoDemand();
                }
                else if (demand != null && demand > 0)
                {
                    marketSign.setDemand(demand);
                }
                else
                {
                    context.sendTranslated("&cInvalid demand amount!");
                }
            }
        }
        if (context.hasFlag("admin"))
        {
            if (MarketSignPerm.SIGN_CREATE_ADMIN.isAuthorized(user))
            {
                marketSign.setAdminSign();
            }
            else
            {
                context.sendTranslated("&cYou are not allowed to create admin-signs");
            }
        }
        if (context.hasFlag("user"))
        {
            if (MarketSignPerm.SIGN_CREATE_USER.isAuthorized(user))
            {
                marketSign.setOwner(user);
                if (marketSign.hasInfiniteSize())
                {
                    marketSign.setSize(6);
                }
            }
            else
            {
                context.sendTranslated("&cYou are not allowed to create user-signs");
            }
        }
        if (context.hasParam("owner"))
        {
            if (MarketSignPerm.SIGN_CREATE_USER_OTHER.isAuthorized(user))
            {
                User owner = context.getParam("owner",null);
                if (owner == null)
                {
                    user.sendTranslated("&cUser %s not found!", context.getString("owner"));
                }
                else
                {
                    marketSign.setOwner(owner);
                }
            }
            else
            {
                context.sendTranslated("&cYou are not allowed to create user-signs for other users");
            }
        }
        if (context.hasFlag("stock"))
        {
            if (marketSign.isAdminSign())
            {
                if (marketSign.hasStock())
                {
                    if (this.config.allowAdminNoStock)
                    {
                        if (MarketSignPerm.SIGN_CREATE_ADMIN_NOSTOCK.isAuthorized(user))
                        {
                            marketSign.setNoStock();
                        }
                        else
                        {
                            context.sendTranslated("&cYou are not allowed to create admin-signs with no stock");
                        }
                    }
                    else
                    {
                        context.sendTranslated("&cAdmin-signs without stock are not allowed!");
                    }
                }
                else
                {
                    if (this.config.allowAdminStock)
                    {
                        if (MarketSignPerm.SIGN_CREATE_ADMIN_STOCK.isAuthorized(user))
                        {
                            marketSign.setStock(0);
                        }
                        else
                        {
                            context.sendTranslated("&cYou are not allowed to create admin-signs with stock");
                        }
                    }
                    else
                    {
                        context.sendTranslated("&cAdmin-signs with stock are not allowed!");
                    }
                }
            }
            else
            {
                context.sendTranslated("&cUser signs cannot have no stock!");
            }
        }
        if (context.hasParam("setstock"))
        {
            if (MarketSignPerm.SIGN_SETSTOCK.isAuthorized(user))
            {
                if (marketSign.hasStock())
                {
                    marketSign.setStock(context.getParam("setstock",0));
                    marketSign.syncOnMe = true;
                }
                else
                {
                    context.sendTranslated("&cThis sign has no stock! Use \"stock\" first to enable it!");
                }
            }
            else
            {
                context.sendTranslated("&cYou are not allowed to set the stock!");
            }
        }
        if (context.hasParam("price"))
        {
            Double dPrice = marketSign.economy.parse(context.getString("price"));
            if (dPrice == null)
            {
                user.sendTranslated("&cInvalid price!");
                marketSign.setPrice(0);
            }
            else if (dPrice < 0)
            {
                user.sendTranslated("&cA negative price!? Are you serious?");
            }
            else
            {
                marketSign.setPrice((long)(dPrice * marketSign.economy.fractionalDigitsFactor()));
            }
        }
        if (context.hasParam("amount"))
        {
            Integer amount = context.getParam("amount",null);
            if (amount == null)
            {
                user.sendTranslated("&cInvalid amount %s!", context.getString("amount"));
            }
            else if (amount < 0)
            {
                user.sendTranslated("&cNegative amounts could be unfair!");
            }
            else
            {
                marketSign.setAmount(amount);
            }
        }
        if (context.hasParam("item"))
        {
            ItemStack item = context.getParam("item", null);
            if (item == null)
            {
                user.sendTranslated("&cItem not found!");
            }
            else
            {
                if (marketSign.isAdminSign())
                {
                    marketSign.setItemStack(item, false);
                }
                else
                {
                    if (marketSign.hasStock() && marketSign.getStock() != 0)
                    {
                        user.sendTranslated("&cYou have to take all items out of the market-sign to be able to change the item in it!");
                        return null;
                    }
                }
            }
        }
        if (context.hasParam("size"))
        {
            if (MarketSignPerm.SIGN_SIZE_CHANGE.isAuthorized(user))
            {
                Integer size = context.getParam("size",null);
                if (size == null || size == 0 || size > 6 || size < -1)
                {
                    context.sendTranslated("&cInvalid size! Use -1 for infinite OR 1-6 inventory-lines!");
                }
                else
                {
                    if (size == -1 && !MarketSignPerm.SIGN_SIZE_CHANGE_INFINITE.isAuthorized(user))
                    {
                        context.sendTranslated("&cYou are not allowed to set infinite inventories!");
                    }
                    else
                    {
                        if (marketSign.isAdminSign())
                        {
                            int maxAdmin = this.config.maxAdminStock;
                            if (maxAdmin != -1 && (size > maxAdmin || size == -1))
                            {
                                context.sendTranslated("&cThe maximum size of admin-signs is set to &6%d&c!", maxAdmin);
                            }
                            else
                            {
                                marketSign.setSize(size);
                                marketSign.syncOnMe = true;
                            }
                        }
                        else // user-sign
                        {
                            int maxUser = this.config.maxUserStock;
                            if (maxUser != -1 && (size > maxUser || size == -1))
                            {
                                context.sendTranslated("&cThe maximum size of user-signs is set to &6%d&c!", maxUser);
                            }
                            else
                            {
                                marketSign.setSize(size);
                                marketSign.syncOnMe = true;
                            }
                        }
                    }
                }
            }
            else
            {
                context.sendTranslated("&cYou are not allowed to change the sign-inventory-size.");
            }
        }
        if (context.hasFlag("exit"))
        {
            this.removeUser(user);
            this.previousMarketSign.put(user.getId(), marketSign);
            this.currentSignLocation.remove(user.getId());
            marketSign.exitEditMode(user);
            return null;
        }
        marketSign.showInfo(user);
        marketSign.updateSignText();
        return null;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onClick(PlayerInteractEvent event)
    {
        if (event.useItemInHand().equals(Event.Result.DENY)) return;
        User user = this.getModule().getCore().getUserManager().getExactUser(event.getPlayer().getName());
        if (!this.hasUser(user))
        {
            return;
        }
        if (event.getAction().equals(Action.LEFT_CLICK_BLOCK))
        {
            if (event.getClickedBlock().getState() instanceof Sign)
            {
                event.setCancelled(true);
                event.setUseItemInHand(Event.Result.DENY);
                Location newLoc = event.getClickedBlock().getLocation();
                if (!newLoc.equals(this.currentSignLocation.get(user.getId())))
                {
                    if (this.currentSignLocation.valueCollection().contains(newLoc))
                    {
                        user.sendTranslated("&cSomeone else is editing this sign!");
                        return;
                    }
                }
                MarketSign curSign = this.signFactory.getSignAt(newLoc);
                if (curSign == null)
                {
                    if (!user.isSneaking())
                    {
                        user.sendTranslated("&cThis is not a market-sign!\n&eUse shift leftclick to convert the sign.");
                        return;
                    }
                    curSign = this.signFactory.createSignAt(user, newLoc);
                    this.setEditingSign(user, curSign);
                    return;
                }
                if (curSign.isInEditMode())
                {
                    if (curSign.tryBreak(user))
                    {
                        this.previousMarketSign.put(user.getId(), curSign);
                        this.currentSignLocation.remove(user.getId());
                    }
                    return;
                }
                if (!MarketSignPerm.SIGN_EDIT.isAuthorized(user))
                {
                    user.sendTranslated("&cYou are not allowed to edit market-signs!");
                    return;
                }
                this.setEditingSign(user, curSign);
            }
        }
        else
        {
            if (event.getPlayer().isSneaking()) return;
            BlockState signFound = null;
            if (event.getAction().equals(Action.RIGHT_CLICK_AIR))
            {
                if (event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getTypeId() != 0)
                {
                    signFound = MarketSignListener.getTargettedSign(event.getPlayer());
                }
            }
            else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getClickedBlock().getState() instanceof Sign)
            {
                signFound = event.getClickedBlock().getState();
            }
            if (signFound == null) return;
            event.setCancelled(true);
            event.setUseItemInHand(Event.Result.DENY);
            Location curLoc = signFound.getLocation();
            MarketSign curSign = this.signFactory.getSignAt(curLoc);
            if (curSign == null)
            {
                user.sendTranslated("&eThis sign is not a market-sign!");
                return; // not a market-sign
            }
            if (!this.setEditingSign(user, curSign))
            {
                if (user.getItemInHand() == null || user.getItemInHand().getTypeId() == 0) return;
                if (!curSign.isAdminSign() && curSign.hasStock() && curSign.getStock() != 0)
                {
                    user.sendTranslated("&cYou have to take all items out of the market-sign to be able to change the item in it!");
                    return;
                }
                curSign.setItemStack(user.getItemInHand(), true);
                curSign.updateSignText();
                user.sendTranslated("&aItem in sign updated!");
            }
        }
    }

    @EventHandler
    public void onSignPlace(BlockPlaceEvent event)
    {
        if (event.getBlockPlaced().getState() instanceof Sign)
        {
            User user = this.getModule().getCore().getUserManager().getExactUser(event.getPlayer().getName());
            if (this.hasUser(user))
            {
                if (!MarketSignPerm.SIGN_CREATE.isAuthorized(user))
                {
                    user.sendTranslated("&cYou are not allowed to create market-signs!");
                    event.setCancelled(true);
                    return;
                }
                this.setEditingSign(user, this.signFactory.createSignAt(user, event.getBlockPlaced().getLocation()));
            }
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event)
    {
        User user = this.getModule().getCore().getUserManager().getExactUser(event.getPlayer().getName());
        if (this.hasUser(user))
        {
            Location loc = event.getBlock().getLocation();
            if (loc.equals(this.currentSignLocation.get(user.getId())))
            {
                event.setCancelled(true);
            }
        }
    }
}
