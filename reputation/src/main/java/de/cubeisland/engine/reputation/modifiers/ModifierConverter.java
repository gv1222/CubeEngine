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
package de.cubeisland.engine.reputation.modifiers;

import java.util.HashMap;
import java.util.Map;

import de.cubeisland.engine.configuration.codec.ConverterManager;
import de.cubeisland.engine.configuration.convert.Converter;
import de.cubeisland.engine.configuration.exception.ConversionException;
import de.cubeisland.engine.configuration.node.MapNode;
import de.cubeisland.engine.configuration.node.Node;
import de.cubeisland.engine.reputation.Reputation;

public class ModifierConverter implements Converter<Modifier>
{
    private Reputation module;

    private Map<String, Class<? extends Modifier>> modifiers = new HashMap<>();
    private Map<Class<? extends Modifier>, String> modifierNames = new HashMap<>();

    public ModifierConverter(Reputation module)
    {
        this.module = module;
        this.modifiers.put("vote", VoteModifier.class);
    }

    private void addModifier(String name, Class<? extends Modifier> modifier)
    {
        this.modifiers.put(name, modifier);
        this.modifierNames.put(modifier, name);
    }

    // TODO

    @Override
    public Node toNode(Modifier object, ConverterManager manager) throws ConversionException
    {
        MapNode mapNode = MapNode.emptyMap();
        mapNode.setExactNode(this.modifierNames.get(object.getClass()), object.toNode(manager));
        return mapNode;
    }

    @Override
    public Modifier fromNode(Node node, ConverterManager manager) throws ConversionException
    {
        if (node instanceof MapNode)
        {
            String key = ((MapNode)node).getFirstKey();
            Class<? extends Modifier> clazz = this.modifiers.get(key);
            if (clazz == null)
            {
                throw ConversionException.of(this, node, key + " is not a valid modifier!");
            }
            try
            {
                Modifier modifier = clazz.newInstance();
                modifier.init(this.module, ((MapNode)node).getMappedNodes().get(key));
                return modifier;
            }
            catch (ReflectiveOperationException e)
            {
                throw new IllegalStateException(e);
            }
        }
        throw ConversionException.of(this, node, "Node is not a MapNode");
    }
}
