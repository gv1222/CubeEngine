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
package de.cubeisland.engine.locker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.entity.EntityType;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.config.node.ListNode;
import de.cubeisland.engine.locker.storage.LockType;
import de.cubeisland.engine.locker.storage.ProtectedType;
import de.cubeisland.engine.core.config.node.BooleanNode;
import de.cubeisland.engine.core.config.node.MapNode;
import de.cubeisland.engine.core.config.node.Node;
import de.cubeisland.engine.core.config.node.NullNode;
import de.cubeisland.engine.core.config.node.StringNode;
import de.cubeisland.engine.core.util.convert.ConversionException;
import de.cubeisland.engine.core.util.convert.Converter;
import de.cubeisland.engine.core.util.matcher.Match;
import de.cubeisland.engine.locker.storage.ProtectionFlags;

public class EntityLockerConfiguration
{
    protected final ProtectedType protectedType;
    protected boolean autoProtect = false;
    protected LockType autoProtectType = LockType.PRIVATE; // defaults to private
    protected List<ProtectionFlags> defaultFlags;
    private final EntityType entityType;
    private boolean enable = true;

    public EntityLockerConfiguration(EntityType entityType)
    {
        this.protectedType = ProtectedType.getProtectedType(entityType);
        this.entityType = entityType;
    }

    public String getTitle()
    {
        return entityType.name();
    }

    public EntityLockerConfiguration autoProtect(LockType type)
    {
        this.autoProtectType = type;
        this.autoProtect = type != null;
        return this;
    }

    public boolean isType(EntityType type)
    {
        return this.entityType.equals(type);
    }

    public static class EntityLockerConfigConverter implements Converter<EntityLockerConfiguration>
    {
        @Override
        public Node toNode(EntityLockerConfiguration object) throws ConversionException
        {
            MapNode root = MapNode.emptyMap();
            MapNode config = MapNode.emptyMap();
            if (!object.enable)
            {
                config.setNode(StringNode.of("enable"), BooleanNode.falseNode());
            }
            if (object.autoProtect)
            {
                config.setNode(StringNode.of("auto-protect"), StringNode.of(object.autoProtectType.name()));
                if (object.defaultFlags != null && !object.defaultFlags.isEmpty())
                {
                    ListNode flags = ListNode.emptyList();
                    for (ProtectionFlags defaultFlag : object.defaultFlags)
                    {
                        flags.addNode(StringNode.of(defaultFlag.name()));
                    }
                    config.setNode(StringNode.of("default-flags"), flags);
                }
            }
            if (config.isEmpty())
            {
                return StringNode.of(object.getTitle());
            }
            root.setNode(StringNode.of(object.getTitle()), config);
            return root;
        }

        private EntityLockerConfiguration fromString(String s) throws ConversionException
        {
            EntityType entityType;
            try
            {
                entityType = EntityType.valueOf(s);
            }
            catch (IllegalArgumentException ignore)
            {
                try
                {
                    entityType = EntityType.fromId(Integer.valueOf(s));
                }
                catch (NumberFormatException ignoreToo)
                {
                    entityType = Match.entity().any(s);
                }
            }
            if (entityType == null)
            {
                throw new ConversionException(s + " is not a valid EntityType!");
            }
            return new EntityLockerConfiguration(entityType);
        }

        @Override
        public EntityLockerConfiguration fromNode(Node node) throws ConversionException
        {
            if (node instanceof NullNode) return null;
            EntityLockerConfiguration configuration;
            if (node instanceof StringNode)
            {
                configuration = fromString(node.unwrap());
            }
            else
            {
                MapNode root = (MapNode)node;
                if (root.isEmpty()) return null;
                String next = root.getOriginalKey(root.getMappedNodes().keySet().iterator().next());
                MapNode config = (MapNode)root.getExactNode(next);
                configuration = fromString(next);
                for (Entry<String, Node> entry : config.getMappedNodes().entrySet())
                {
                    if (entry.getKey().equals("enable"))
                    {
                        configuration.enable = ((BooleanNode)entry.getValue()).getValue();
                    }
                    if (entry.getKey().equals("auto-protect"))
                    {
                        configuration.autoProtect = true;
                        configuration.autoProtectType = LockType.valueOf(entry.getValue().unwrap());
                    }
                    if (entry.getKey().equals("default-flags"))
                    {
                        ListNode list = (ListNode)entry.getValue();
                        configuration.defaultFlags = new ArrayList<>();
                        for (Node listedNode : list.getListedNodes())
                        {
                            ProtectionFlags flag = ProtectionFlags.valueOf(listedNode.unwrap());
                            if (configuration.protectedType.supportedFlags.contains(flag))
                            {
                                configuration.defaultFlags.add(flag);
                            }
                            else
                            {
                                CubeEngine.getCore().getLog().warn("[Locker] Unsupported flag for protectedType! {}: {}", configuration.protectedType.name(), flag.name());
                            }
                        }
                    }
                }
            }
            return configuration;
        }
    }
}
