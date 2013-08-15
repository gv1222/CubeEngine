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
package de.cubeisland.engine.core.config.node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import de.cubeisland.engine.core.util.convert.Convert;

public class ListNode extends ParentNode
{

    private ArrayList<Node> listedNodes = new ArrayList<>();

    public ListNode(Iterable list)
    {
        if (list != null)
        {
            for (Object object : list)
            {
                Node node = Convert.wrapIntoNode(object);
                node.setParentNode(this);
                listedNodes.add(node);
            }
        }
    }

    public ListNode(Object[] array)
    {
        if (array != null)
        {
            for (Object object : array)
            {
                Node node = Convert.wrapIntoNode(object);
                node.setParentNode(this);
                listedNodes.add(node);
            }
        }
    }

    private ListNode()
    {}

    public ArrayList<Node> getListedNodes()
    {
        return listedNodes;
    }

    public static ListNode emptyList()
    {
        return new ListNode();
    }

    public void addNode(Node node)
    {
        this.listedNodes.add(node);
        node.setParentNode(this);
    }

    @Override
    protected Node setExactNode(String key, Node node)
    {
        if (key.startsWith("["))
        {
            try
            {
                int pos = Integer.valueOf(key.substring(1));
                node.setParentNode(this);
                return this.listedNodes.set(pos, node);
            }
            catch (NumberFormatException ex)
            {
                throw new IllegalArgumentException("Cannot set Node! Could not parse ListPath", ex);
            }
            catch (IndexOutOfBoundsException ex)
            {
                throw new IllegalArgumentException("Cannot set Node! Out of Range!", ex);
            }
        }
        else
        {
            throw new IllegalArgumentException("Cannot set Node! ListPath has to start with [!");
        }
    }

    @Override
    public Node getExactNode(String key)
    {
        if (key.startsWith("["))
        {
            try
            {
                int pos = Integer.valueOf(key.substring(1));
                return this.listedNodes.get(pos);
            }
            catch (NumberFormatException ex)
            {
                throw new IllegalArgumentException("Cannot get Node! Could not parse ListPath", ex);
            }
            catch (IndexOutOfBoundsException ex)
            {
                throw new IllegalArgumentException("Cannot get Node! Out of Range!", ex);
            }
        }
        else
        {
            throw new IllegalArgumentException("Cannot get Node! ListPath has to start with [!");
        }
    }

    @Override
    protected Node removeExactNode(String key)
    {
        if (key.startsWith("["))
        {
            try
            {
                int pos = Integer.valueOf(key.substring(1));
                return this.listedNodes.remove(pos);
            }
            catch (NumberFormatException ex)
            {
                throw new IllegalArgumentException("Cannot remove Node! Could not parse ListPath!", ex);
            }
            catch (IndexOutOfBoundsException ex)
            {
                throw new IllegalArgumentException("Cannot remove Node! Out of Range!", ex);
            }
        }
        else
        {
            throw new IllegalArgumentException("Cannot remove Node! ListPath has to start with [!");
        }
    }

    public Node setNode(IntNode keyNode, Node node)
    {
        return this.setExactNode("[" + keyNode.getValue(), node);
    }

    @Override
    public boolean isEmpty()
    {
        return this.listedNodes.isEmpty();
    }

    @Override
    public void cleanUpEmptyNodes()
    {
        Set<Node> nodesToRemove = new HashSet<>();
        for (Node node : this.getListedNodes())
        {
            if (node instanceof ParentNode)
            {
                ((ParentNode) node).cleanUpEmptyNodes();
                if (((ParentNode) node).isEmpty())
                {
                    nodesToRemove.add(node);
                }
            }
        }
        this.listedNodes.removeAll(nodesToRemove);
    }

    @Override
    protected String getPath(Node node, String path, String pathSeparator)
    {
        int pos = this.listedNodes.indexOf(node);
        if (pos == -1)
        {
            throw new IllegalArgumentException("Parented node not in list!");
        }
        if (path.isEmpty())
        {
            path = "[" + pos;
        }
        else
        {
            path = "[" + pos + pathSeparator + path;
        }
        if (this.getParentNode() != null)
        {
            return this.getParentNode().getPath(this, path, pathSeparator);
        }
        return path;
    }
}
