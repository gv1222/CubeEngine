package de.cubeisland.CubeWar.Area;

import de.cubeisland.CubeWar.CubeWar;
import de.cubeisland.CubeWar.Groups.Group;
import de.cubeisland.CubeWar.Groups.GroupControl;
import gnu.trove.map.hash.THashMap;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Chunk;
import org.bukkit.Location;

/**
 *
 * @author Faithcaio
 */
public class Area {

    private static THashMap<Chunk,Group> chunks = new THashMap<Chunk,Group>();
    
    public Area() {}
    
    public static Group addChunk(Location loc, Group group)
    {
        return addChunk(loc.getChunk(), group);
    }
    
    public static Group addChunk(Chunk chunk, Group group)
    {
        CubeWar.debug("ADD X: "+chunk.getX()+" Z:"+chunk.getZ()+" "+chunks.get(chunk)+" -->"+group);
        if (!(group.equals(chunks.get(chunk))))
        {
            if (chunks.get(chunk) == null)
            {
                group.addPower_used();
            }
            else
            {
                group.addPower_used();
                chunks.get(chunk).remPower_used();
            }
        }
        return chunks.put(chunk, group);
    }
    
    public static Group getGroup(Location loc)
    {
        Group tmp = chunks.get(loc.getChunk());
        if (tmp == null)
            return GroupControl.getWildLand();
        return chunks.get(loc.getChunk());
    }
    
    public static Group remChunk(Location loc)
    {
        return chunks.remove(loc.getChunk());
    }
    
    public static Group remChunk(Chunk chunk)
    {
        CubeWar.debug("REM X: "+chunk.getX()+" Z:"+chunk.getZ()+" "+chunks.get(chunk));
        Group group = chunks.remove(chunk);
        if (group != null)
            group.remPower_used();
        return group;
    }
    
    public static void remAll(Group group)
    {
        List<Chunk> remlist = new ArrayList<Chunk>();
        for (Chunk chunk : chunks.keySet())
        {
            if (chunks.get(chunk).equals(group))
                remlist.add(chunk);
                
        }
        for (Chunk chunk : remlist)
        {
            chunks.remove(chunk);
        }
        group.resetPower_used();
    }
    
    public static void remAllAll()
    {
        chunks.clear();
    }
}
