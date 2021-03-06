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
package de.cubeisland.engine.core.bukkit;

import java.util.ArrayList;
import java.util.Set;
import java.util.Collection;
import java.util.Map.Entry;

import net.minecraft.server.v1_7_R1.NBTBase;
import net.minecraft.server.v1_7_R1.NBTTagByte;
import net.minecraft.server.v1_7_R1.NBTTagByteArray;
import net.minecraft.server.v1_7_R1.NBTTagCompound;
import net.minecraft.server.v1_7_R1.NBTTagDouble;
import net.minecraft.server.v1_7_R1.NBTTagEnd;
import net.minecraft.server.v1_7_R1.NBTTagFloat;
import net.minecraft.server.v1_7_R1.NBTTagInt;
import net.minecraft.server.v1_7_R1.NBTTagIntArray;
import net.minecraft.server.v1_7_R1.NBTTagList;
import net.minecraft.server.v1_7_R1.NBTTagLong;
import net.minecraft.server.v1_7_R1.NBTTagShort;
import net.minecraft.server.v1_7_R1.NBTTagString;
import net.minecraft.server.v1_7_R1.TileEntity;
import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;

import org.bukkit.Location;

import de.cubeisland.engine.configuration.node.Node;
import de.cubeisland.engine.configuration.node.ByteNode;
import de.cubeisland.engine.configuration.node.DoubleNode;
import de.cubeisland.engine.configuration.node.FloatNode;
import de.cubeisland.engine.configuration.node.IntNode;
import de.cubeisland.engine.configuration.node.ListNode;
import de.cubeisland.engine.configuration.node.LongNode;
import de.cubeisland.engine.configuration.node.MapNode;
import de.cubeisland.engine.configuration.node.NullNode;
import de.cubeisland.engine.configuration.node.ShortNode;
import de.cubeisland.engine.configuration.node.StringNode;

// TODO FIXME NBT has changed...
public class NBTUtils
{
    public static NBTTagCompound getTileEntityNBTAt(Location location)
    {
        NBTTagCompound result = new NBTTagCompound();
        TileEntity tileEntity = ((CraftWorld)location.getWorld()).getHandle()
             .getTileEntity(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        if (tileEntity == null) return null;
        tileEntity.b(result);
        return result;
    }

    public static void setTileEntityNBTAt(Location location, NBTTagCompound nbtData)
    {
        TileEntity tileEntity =  ((CraftWorld)location.getWorld()).getHandle()
                      .getTileEntity(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        tileEntity.a(nbtData);
    }

    @SuppressWarnings("unchecked")
    public static Node convertNBTToNode(NBTBase nbtBase)
    {
        if (nbtBase == null) return null;
        if (nbtBase instanceof NBTTagEnd)
        {
            return NullNode.emptyNode();
        }
        if (nbtBase instanceof NBTTagByte)
        {
            return new ByteNode(((NBTTagByte)nbtBase).f());
        }
        if (nbtBase instanceof NBTTagShort)
        {
            return new ShortNode(((NBTTagShort)nbtBase).e());
        }
        if (nbtBase instanceof NBTTagInt)
        {
            return  new IntNode(((NBTTagInt)nbtBase).d());
        }
        if (nbtBase instanceof NBTTagLong)
        {
            return new LongNode(((NBTTagLong)nbtBase).c());
        }
        if (nbtBase instanceof NBTTagFloat)
        {
            return new FloatNode(((NBTTagFloat)nbtBase).h());
        }
        if (nbtBase instanceof NBTTagDouble)
        {
            return new DoubleNode(((NBTTagDouble)nbtBase).g());
        }
        if (nbtBase instanceof NBTTagByteArray)
        {
            ListNode list = ListNode.emptyList();
            for (byte b : ((NBTTagByteArray)nbtBase).c())
            {
                list.addNode(new ByteNode(b));
            }
            return list;
        }
        if (nbtBase instanceof NBTTagString)
        {
            return StringNode.of(((NBTTagString)nbtBase).a_());
        }
        if (nbtBase instanceof NBTTagList)
        {
            ListNode list = ListNode.emptyList();
            for (int i = 0; i < ((NBTTagList)nbtBase).size(); i++)
            {
                list.addNode(convertNBTToNode(((NBTTagList)nbtBase).get(i)));
            }
            return list;
        }
        if (nbtBase instanceof NBTTagCompound)
        {
            MapNode map = MapNode.emptyMap();
            NBTTagCompound compound = (NBTTagCompound)nbtBase;
            for (String key : (Set<String>)compound.c())
            {
                map.setExactNode(key,convertNBTToNode(compound.get(key)));
            }
            return map;
        }
        if (nbtBase instanceof NBTTagIntArray)
        {
            ListNode list = ListNode.emptyList();
            for (int i : ((NBTTagIntArray)nbtBase).c())
            {
                list.addNode(new IntNode(i));
            }
            return list;
        }
        throw new IllegalStateException("Unknown NbtTag-Type! "+ nbtBase.getClass().getName());
    }

    // TODO verify this
    @SuppressWarnings("unchecked")
    public static NBTBase convertNodeToNBT(Node node)
    {
        if (node instanceof NullNode)
        {
            return HelperNBTBase.createEnd();
        }
        if (node instanceof ByteNode)
        {
            return new NBTTagByte((Byte)node.getValue());
        }
        if (node instanceof ShortNode)
        {
            return new NBTTagShort((Short)node.getValue());
        }
        if (node instanceof IntNode)
        {
            return new NBTTagInt((Integer)node.getValue());
        }
        if (node instanceof LongNode)
        {
            return new NBTTagLong((Long)node.getValue());
        }
        if (node instanceof FloatNode)
        {
            return new NBTTagFloat((Float)node.getValue());
        }
        if (node instanceof DoubleNode)
        {
            return new NBTTagDouble((Double)node.getValue());
        }
        if (node instanceof ListNode)
        {
            boolean onlyByte = true;
            boolean onlyInt = true;
            ArrayList<Node> listedNodes = ((ListNode)node).getListedNodes();
            for (Node listedNode : listedNodes)
            {
                if (!(listedNode instanceof IntNode))
                {
                    onlyInt = false;
                }
                if (!(listedNode instanceof ByteNode))
                {
                    onlyByte = false;
                }
            }
            if (onlyByte)
            {
                byte[] byteArray = new byte[listedNodes.size()];
                for (int i = 0 ; i < byteArray.length ; i++)
                {
                    byteArray[i] = (Byte)listedNodes.get(i).getValue();
                }
                return new NBTTagByteArray(byteArray);
            }
            if (onlyInt)
            {
                int[] intarray = new int[listedNodes.size()];
                for (int i = 0 ; i < intarray.length ; i++)
                {
                    intarray[i] = (Integer)listedNodes.get(i).getValue();
                }
                return new NBTTagIntArray(intarray);
            }
            NBTTagList list = new NBTTagList();
            for (Node listedNode : listedNodes)
            {
                list.add(convertNodeToNBT(listedNode));
            }
            return list;

        }
        if (node instanceof StringNode)
        {
            return new NBTTagString((String)node.getValue());
        }
        if (node instanceof MapNode)
        {
            NBTTagCompound compound = new NBTTagCompound();
            for (Entry<String, Node> entry : ((MapNode)node).getMappedNodes().entrySet())
            {
                compound.set(((MapNode)node).getOriginalKey(entry.getKey()),convertNodeToNBT(entry.getValue()));
            }
            return compound;
        }
        throw new IllegalStateException("Cannot convert nodes to NbtTags! "+ node.getClass().getName());
    }

    private abstract static class HelperNBTBase extends NBTBase
    {
        public static NBTTagEnd createEnd()
        {
            return (NBTTagEnd)NBTBase.createTag((byte)0);
        }
    }
}
