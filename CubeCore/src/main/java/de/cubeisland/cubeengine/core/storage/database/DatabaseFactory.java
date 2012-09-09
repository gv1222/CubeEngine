package de.cubeisland.cubeengine.core.storage.database;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.storage.DatabaseConfiguration;
import de.cubeisland.cubeengine.core.util.Validate;
import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Level;

/**
 *
 * @author Anselm Brehme
 */
public class DatabaseFactory
{
    private static final HashMap<String, Class<? extends DatabaseConfiguration>> databases = new HashMap<String, Class<? extends DatabaseConfiguration>>();

    public static Database loadDatabase(String name, File configFile)
    {
        Validate.notNull(name, "The name must not be null!");
        Validate.notNull(configFile, "The config file must not be null!");
        
        Class<? extends DatabaseConfiguration> configClazz = databases.get(name.toLowerCase(Locale.ENGLISH));
        try
        {
            DatabaseConfiguration config = Configuration.load(configClazz, configFile);
            return config.getDatabaseClass().getConstructor(DatabaseConfiguration.class).newInstance(config);
        }
        catch (Exception e)
        {
            CubeEngine.getLogger().log(Level.SEVERE, "Couldn't establish the database connection: " + e.getLocalizedMessage(), e);
            return null;
        }
    }

    public static void registerDatabase(String name, Class<? extends DatabaseConfiguration> clazz)
    {
        databases.put(name.toLowerCase(Locale.ENGLISH), clazz);
    }
}