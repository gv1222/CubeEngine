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
package de.cubeisland.engine.log.action.logaction.interact;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.engine.log.storage.LogEntry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gnu.trove.set.hash.TShortHashSet;

import static de.cubeisland.engine.log.action.ActionTypeCategory.*;

/**
 * splashed potions
 * <p>Events: {@link PotionSplashEvent}</p>
 */
public class PotionSplash extends SimpleLogActionType
{
    @Override
    protected Set<ActionTypeCategory> getCategories()
    {
        return new HashSet<>(Arrays.asList(PLAYER, ENTITY, ITEM));
    }

    @Override
    public boolean canRollback()
    {
        return false;
    }
    @Override
    public String getName()
    {
        return "potion-splash";
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event)
    {
        if (this.isActive(event.getPotion().getWorld()))
        {
            LivingEntity livingEntity = event.getPotion().getShooter();
            if (livingEntity == null)
            {
                // TODO dispener?
                return;
            }
            String additionalData = this.serializePotionLog(event);
            this.logSimple(livingEntity,additionalData);
        }
    }

    public String serializePotionLog(PotionSplashEvent event)
    {
        ObjectNode json = this.om.createObjectNode();
        ArrayNode effects = json.putArray("effects");
        for (PotionEffect potionEffect : event.getPotion().getEffects())
        {
            ArrayNode effect = effects.addArray();
            effect.add(potionEffect.getType().getName());
            effect.add(potionEffect.getAmplifier());
            effect.add(potionEffect.getDuration());
        }
        if (!event.getAffectedEntities().isEmpty())
        {
            json.put("amount", event.getAffectedEntities().size());
            ArrayNode affected = json.putArray("affected");
            TShortHashSet set = new TShortHashSet();
            for (LivingEntity livingEntity : event.getAffectedEntities())
            {
                if (livingEntity instanceof Player)
                {
                    User user = um.getExactUser(((Player)livingEntity).getName());
                    affected.add(user.getId());
                    continue;
                }
                short entity = livingEntity.getType().getTypeId();
                if (!set.contains(entity))
                {
                    affected.add(-entity);
                    set.add(entity);
                }
            }
        }
        this.logModule.getLog().debug(json.toString());
        return json.toString();
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        String effects;
        Iterator<JsonNode> it = logEntry.getAdditional().get("effects").elements();
        effects = "&6"+it.next().iterator().next().asText();
        while (it.hasNext())
        {
            JsonNode next = it.next();
            effects += "&f, &6";
            effects += next.iterator().next().asText();
        }
        effects = ChatFormat.parseFormats(effects);
        int amountAffected = 0;
        if (logEntry.getAdditional().get("amount") != null)
        {
            amountAffected = logEntry.getAdditional().get("amount").asInt();
        }
        user.sendTranslated("%s&2%s&a used a &6splash potion&a &f(%s&f)&a onto &6%d&a entities%s", time, logEntry
            .getCauserUser().getName(), effects, amountAffected, loc);
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        //TODO
        return false;
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).POTION_SPLASH_enable;
    }
}
