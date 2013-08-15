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
package de.cubeisland.engine.test;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import net.minecraft.server.v1_6_R2.DedicatedPlayerList;
import net.minecraft.server.v1_6_R2.EntityPlayer;
import net.minecraft.server.v1_6_R2.Packet0KeepAlive;
import org.bukkit.craftbukkit.v1_6_R2.CraftServer;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import de.cubeisland.engine.basics.Basics;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.bukkit.BukkitCore;
import de.cubeisland.engine.core.bukkit.PlayerLanguageReceivedEvent;
import de.cubeisland.engine.core.command.reflected.ReflectedCommand;
import de.cubeisland.engine.core.config.Configuration;
import de.cubeisland.engine.core.filesystem.FileUtil;
import de.cubeisland.engine.core.module.Inject;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.user.UserManager;
import de.cubeisland.engine.core.util.matcher.Match;
import de.cubeisland.engine.test.commands.TestCommands;

public class Test extends Module
{
    public static final String TEST_WORLD_NAME = "world_test";
    public UserManager uM;
    protected TestConfig config;
    public static List<String> aListOfPlayers;
    @Inject public Basics basicsModule;
    private Timer timer;

    @Override
    public void onLoad()
    {
        this.getCore().getWorldManager().registerGenerator(this, "test", new TestGenerator());
    }

    @Override
    public void onStartupFinished()
    {
        World world = getCore().getWorldManager().createWorld(WorldCreator.name(TEST_WORLD_NAME)
                                                                          .generator("CubeEngine:test:test")
                                                                          .generateStructures(false)
                                                                          .type(WorldType.FLAT)
                                                                          .environment(Environment.NORMAL).seed(1231));
        if (world != null)
        {
            world.setAmbientSpawnLimit(0);
            world.setAnimalSpawnLimit(0);
            world.setMonsterSpawnLimit(0);
            world.setSpawnFlags(false, false);
        }
    }

    @Override
    public void onEnable()
    {
        this.config = Configuration.load(TestConfig.class, this);
        try
        {
            this.config.loadChild(this.getFolder().resolve("childConfig.yml"));
        }
        catch (IllegalStateException ex)
        {
            this.getLog().info("{} does not exist.", this.getFolder().resolve("childConfig.yml"));
        }
        Configuration.load(TestConfig2.class, this.getFolder().resolve("updateConfig.yml"));
        // this.getCore().getFileManager().dropResources(TestRecource.values());
        this.uM = this.getCore().getUserManager();
        try
        {
            this.initializeDatabase();
            this.testDatabase();
        }
        catch (Exception ex)
        {
            this.getLog().error("Error while Enabling the TestModule: " + ex.getLocalizedMessage(), ex);
        }
        this.getCore().getEventManager().registerListener(this, new TestListener(this));

        this.testl18n();
        this.testMatchers();
        this.testsomeUtils();

        this.getCore().getCommandManager().registerCommands(this, new TestCommands(), ReflectedCommand.class);

        this.getCore().getEventManager().registerListener(this, new Listener()
        {
            @EventHandler
            public void onLanguageReceived(PlayerLanguageReceivedEvent event)
            {
                getLog().debug("Player: {} Lang: {}", event.getPlayer().getName(), event.getLanguage());
            }
        });

        this.getLog().debug("Basics-Module: {0}", String.valueOf(this.basicsModule));
        this.getLog().debug("BukkitCore-Plugin: {0}", String.valueOf(this.getCore()));

        CubeEngine.getLog().trace("Trace log on core's logger");
        CubeEngine.getLog().debug("Debug log on core's logger");
        CubeEngine.getLog().info("Info log on core's logger");
        CubeEngine.getLog().warn("Warn log on core's logger");
        CubeEngine.getLog().error("Error log on core's logger");
        this.getLog().trace("Trace log on test's logger");
        this.getLog().debug("Debug log on test's logger");
        this.getLog().info("Info log on test's logger");
        this.getLog().warn("Warn log on test's logger");
        this.getLog().error("Error log on test's logger");

        this.timer = new Timer("keepAliveTimer");
        this.timer.schedule(new KeepAliveTimer(), 2 * 1000, 2 * 1000);
    }

    public void initializeDatabase() throws SQLException
    {//TODO DATABASE
        /*
        Database db = this.getCore().getDB();
        try
        {
            db.execute(db.getQueryBuilder().dropTable("Orders").end());
        }
        catch (Exception ignore)
        {}
        this.manager = new TestManager(db);*/
    }

    @Override
    public void onDisable()
    {
        this.timer.cancel();
        try
        {
            this.getCore().getWorldManager().deleteWorld(TEST_WORLD_NAME);
        }
        catch (IOException e)
        {
            this.getLog().warn("Failed to delete the test world: " + e.getLocalizedMessage(), e);
        }

        this.timer = null;
    }

    private Date getDate(int year, int month, int day)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        return new Date(calendar.getTimeInMillis());
    }

    public void testDatabase() throws SQLException
    {
        //TODO DATABASE
        /*
        Database database = this.getCore().getDB();

        try
        {//Clears the TestLogs in Database (This does always fail with new db)
            database.execute(database.getQueryBuilder().truncateTable("test_log").end());
        }
        catch (Exception ignored)
        {}

        this.manager.store(new TestModel(this.getDate(2012, 8, 8), 10, "Heinz"), false);
        this.manager.store(new TestModel(this.getDate(2012, 6, 8), 30, "Hans"), false);
        this.manager.store(new TestModel(this.getDate(2012, 8, 6), 20, "Manfred"), false);
        this.manager.store(new TestModel(this.getDate(2012, 8, 8), 20, "Heinz"), false);
        this.manager.store(new TestModel(this.getDate(2012, 8, 8), 120, "Hans"), false);
        this.manager.store(new TestModel(this.getDate(2011, 2, 8), 50, "Manfred"), false);
        this.manager.get(2L);
        this.manager.getAll();
        TestModel model = this.manager.get(3L);
        model.orderDate = this.getDate(111, 2, 2);
        model.orderPrice = 100;
        model.customer = "Paul";
        this.manager.update(model);
        */
    }

    public void testl18n()
    {
        this.getLog().debug(CubeEngine.getCore().getI18n().translate("de_DE", "test", "english TEST"));
        this.getLog().debug(CubeEngine.getCore().getI18n().translate("fr_FR", "test", "english TEST"));
    }

    private void testMatchers()
    {
        this.getLog().debug(String.valueOf(Match.enchant().enchantment("infinity")));
        this.getLog().debug(String.valueOf(Match.enchant().enchantment("infini")));
        this.getLog().debug(String.valueOf(Match.enchant().enchantment("hablablubb")) + " is null");
        this.getLog().debug(String.valueOf(Match.enchant().enchantment("protect")));
        this.getLog().debug(String.valueOf(Match.material().itemStack("stone").serialize()));
        this.getLog().debug(String.valueOf(Match.material().itemStack("stoned").serialize()));
        this.getLog().debug(String.valueOf(Match.material().itemStack("hablablubb")) + " is null");
        this.getLog().debug(String.valueOf(Match.material().itemStack("wool:red").serialize()));
        this.getLog().debug(String.valueOf(Match.material().itemStack("35").serialize()));
        this.getLog().debug(String.valueOf(Match.material().itemStack("35:15").serialize()));
        this.getLog().debug(String.valueOf(Match.material().itemStack("35:red").serialize()));
        this.getLog().debug(String.valueOf(Match.material().itemStack("wood:birch").serialize()));
        this.getLog().debug(String.valueOf(Match.material().itemStack("leves:pine").serialize()));
        this.getLog().debug(String.valueOf(Match.material().itemStack("spawnegg:pig").serialize()));
        this.getLog().debug(String.valueOf(Match.entity().any("pig")));
        this.getLog().debug(String.valueOf(Match.entity().monster("zombi")));
        this.getLog().debug(String.valueOf(Match.entity().friendlyMob("shep")));
        this.getLog().debug(String.valueOf(Match.entity().friendlyMob("ghast")) + " is null");
    }

    private void testsomeUtils()
    {
        try
        {
            aListOfPlayers = FileUtil.readStringList(this.getFolder().resolve("testdata").resolve("player.txt"));
        }
        catch (Exception ex)
        {
            this.getLog().error("Error in testsomeutils: " + ex.getLocalizedMessage(), ex);
        }
    }

    private class KeepAliveTimer extends TimerTask
    {
        private final DedicatedPlayerList mojangServer;
        private final Random random;

        public KeepAliveTimer()
        {
            this.mojangServer = ((CraftServer)((BukkitCore)getCore()).getServer()).getHandle();
            this.random = new Random();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void run()
        {
            for (EntityPlayer player : (List<EntityPlayer>)this.mojangServer.players)
            {
                player.playerConnection.sendPacket(new Packet0KeepAlive(random.nextInt()));
            }
        }
    }
}
