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

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import de.cubeisland.engine.configuration.codec.ConverterManager;
import de.cubeisland.engine.configuration.node.IntNode;
import de.cubeisland.engine.configuration.node.Node;
import de.cubeisland.engine.reputation.Reputation;
import de.cubeisland.engine.vote.VoteEvent;

public class VoteModifier implements Modifier, Listener
{
    private int amount;
    private Reputation module;

    public VoteModifier(int amount)
    {
        this.amount = amount;
    }

    @Override
    public void init(Reputation module, Node node)
    {
        this.module = module;
        module.getCore().getEventManager().registerListener(module, this);
        if (node instanceof IntNode)
        {
            this.amount = ((IntNode)node).getValue();
        }
        else
        {
            throw new IllegalStateException("invalid node");
        }
    }

    @EventHandler
    public void onVote(VoteEvent event)
    {
        this.module.modifyReputation(event.getUser(), this.amount);
    }

    @Override
    public Node toNode(ConverterManager manager)
    {
        IntNode intNode = new IntNode(this.amount);
        return intNode;
    }
}
