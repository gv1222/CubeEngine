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
package de.cubeisland.engine.border;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import org.bukkit.Chunk;
import org.bukkit.World;

import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.parameterized.completer.WorldCompleter;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.Triplet;

public class BorderCommands extends ContainerCommand
{
    private Border module;

    public BorderCommands(Border module)
    {
        super(module, "border", "border commands");
        this.module = module;
    }

    private LinkedList<Triplet<Long,Integer,Integer>> chunksToGenerate;
    private LinkedList<Triplet<World,Integer,Integer>> chunksToUnload;
    private CommandSender sender = null;
    private long tickStart;
    private int total = 0;
    private int totalDone = 0;
    private long lastNotify;
    private int generated;
    private boolean running = false;

    @Command(desc = "Sets the center of the border", usage = "[<chunkX> <chunkZ>]|[-spawn] [in <world>]",
    flags = @Flag(longName = "spawn", name = "s"),
    params = @Param(names = {"in", "world", "w"}, type = World.class, completer = WorldCompleter.class))
    public void setCenter(ParameterizedContext context)
    {
        World world;
        if (context.hasParam("in"))
        {
            world = context.getParam("in");
        }
        else if (!(context.getSender() instanceof User))
        {
            context.sendTranslated("&cYou need to specify a world!");
            return;
        }
        else
        {
            world = ((User)context.getSender()).getWorld();
        }
        Chunk center;
        if (context.hasFlag("s"))
        {
            this.module.getConfig(world).center.setCenter(world.getSpawnLocation().getChunk(), true);
            context.sendTranslated("&aCenter for Border in &6%s&a set to world-spawn!", world.getName());
            return;
        }
        else if (context.hasArg(1))
        {
            Integer x = context.getArg(0, Integer.class, null);
            Integer z = context.getArg(0, Integer.class, null);
            if (x == null || z == null)
            {
                context.sendTranslated("&cInvalid Chunk-coordinates!");
                return;
            }
            center = world.getChunkAt(x, z);
        }
        else if (context.getSender() instanceof User)
        {
            center = ((User)context.getSender()).getLocation().getChunk();
        }
        else
        {
            context.sendTranslated("&cYou need to specify the chunk-coordinates or use the -spawn flag");
            return;
        }
        this.module.getConfig(world).center.setCenter(center, false);
        context.sendTranslated("&aCenter for Border in &6%s&a set!", world.getName());
    }

    @Alias(names = "generateBorder")
    @Command(desc = "Generates the chunks located in the border", min = 1, max = 1)
    public void generate(ParameterizedContext context)
    {
        if (running)
        {
            context.sendTranslated("&cChunk generation is already running!");
            return;
        }
        String worldName = context.getString(0);
        this.chunksToGenerate = new LinkedList<>();
        this.chunksToUnload = new LinkedList<>();
        if (worldName.equals("*"))
        {
            for (World world : this.module.getCore().getWorldManager().getWorlds())
            {
                this.addChunksToGenerate(world, context.getSender());
            }
        }
        else
        {
            World world = this.module.getCore().getWorldManager().getWorld(worldName);
            if (world == null)
            {
                context.sendTranslated("&cThere is no world named &6%s", worldName);
                return;
            }
            this.addChunksToGenerate(world, context.getSender());
        }
        this.sender = context.getSender();
        this.total = this.chunksToGenerate.size();
        this.totalDone = 0;
        this.lastNotify = System.currentTimeMillis();
        this.generated = 0;
        this.scheduleGeneration(1);
    }

    private void addChunksToGenerate(World world, CommandSender sender)
    {
        BorderConfig config = this.module.getConfig(world);
        Chunk spawnChunk = world.getSpawnLocation().getChunk();
        final int spawnX = spawnChunk.getX();
        final int spawnZ = spawnChunk.getZ();
        int radius = config.radius;
        radius += sender.getServer().getViewDistance();
        int radiusSquared = radius * radius;
        int chunksAdded = 0;
        long worldID = this.module.getCore().getWorldManager().getWorldId(world);
        // Construct Spiral
        int curLen = 1;
        int curX = spawnX;
        int curZ = spawnZ;
        int dir = 1;
        while (curLen <= radius * 2)
        {
            for (int i = 0; i < curLen; i++)
            {
                curX += dir;
                if (addIfInBorder(config, worldID, curX, curZ, spawnX, spawnZ, radius,  radiusSquared))
                {
                    chunksAdded++;
                }
            }
            for (int i = 0; i < curLen; i++)
            {
                curZ += dir;
                if (addIfInBorder(config, worldID, curX, curZ, spawnX, spawnZ, radius, radiusSquared))
                {
                    chunksAdded++;
                }
            }
            curLen++;
            dir = -dir;
        }
        sender.sendTranslated("&aAdded &6%d &achunks to generate in &6%s", chunksAdded, world.getName());
    }

    private boolean addIfInBorder(BorderConfig config, long worldId, int x, int z, int spawnX, int spawnZ, int radius, int radiusSquared)
    {
        if (config.square)
        {
            if (Math.abs(spawnX - x) <= radius && Math.abs(spawnZ - z) <= radius)
            {
                this.chunksToGenerate.add(new Triplet<>(worldId, x, z));
                return true;
            }
        }
        else if (Math.pow(spawnX - x, 2) + Math.pow(spawnZ - z, 2) <= radiusSquared)
        {
            this.chunksToGenerate.add(new Triplet<>(worldId, x, z));
            return true;
        }
        return false;
    }

    private void scheduleGeneration(int inTicks)
    {
        this.running = true;
        this.module.getCore().getTaskManager().runTaskDelayed(module, new Runnable()
        {
            @Override
            public void run()
            {
                BorderCommands.this.generate();
            }
        }, inTicks);
    }
    private static final int TIMELIMIT = 40;

    private void generate()
    {
        this.tickStart = System.currentTimeMillis();
        Runtime rt = Runtime.getRuntime();
        int freeMemory = (int)((rt.maxMemory() - rt.totalMemory() + rt.freeMemory()) / 1048576);// 1024*1024 = 1048576 (bytes in 1 MB)
        if (freeMemory < 300) // less than 300 MB memory left
        {
            this.scheduleGeneration(20 * 10); // Take a 10 second break
            sender.sendTranslated("&cAvailiable Memory getting low! Pausing ChunkGeneration");
            rt.gc();
            return;
        }
        while (System.currentTimeMillis() - this.tickStart < TIMELIMIT)
        {
            if (chunksToGenerate.isEmpty())
            {
                break;
            }
            Triplet<Long, Integer, Integer> poll = chunksToGenerate.poll();
            World world = this.module.getCore().getWorldManager().getWorld(poll.getFirst());
            if (!world.isChunkLoaded(poll.getSecond(), poll.getThird()))
            {
                if (!world.loadChunk(poll.getSecond(), poll.getThird(), false))
                {
                    world.loadChunk(poll.getSecond(), poll.getThird(), true);
                    generated++;
                }
                this.chunksToUnload.add(new Triplet<>(world, poll.getSecond(), poll.getThird()));
            }
            if (this.chunksToUnload.size() > 8)
            {
                Triplet<World, Integer, Integer> toUnload = chunksToUnload.poll();
                toUnload.getFirst().unloadChunkRequest(toUnload.getSecond(), toUnload.getThird());
            }
            totalDone++;

            if (lastNotify + TimeUnit.SECONDS.toMillis(5) < System.currentTimeMillis())
            {
                this.lastNotify = System.currentTimeMillis();
                int percentNow = totalDone * 100 / total;
                this.sender.sendTranslated("&aChunkgeneration is at &6%d%% &a(&6%d/%d&a)", percentNow, totalDone, total);
            }
        }
        if (!chunksToGenerate.isEmpty())
        {
            this.scheduleGeneration(1);
        }
        else
        {
            for (Triplet<World, Integer, Integer> triplet : chunksToUnload)
            {
                triplet.getFirst().unloadChunkRequest(triplet.getSecond(), triplet.getThird());
            }
            sender.sendTranslated("&aChunkgeneration completed! Generated &6%d&a chunks", generated);
            rt.gc();
            this.running = false;
        }
    }
}
