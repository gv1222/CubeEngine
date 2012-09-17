package de.cubeisland.cubeengine.core;

import de.cubeisland.cubeengine.core.command.CommandManager;
import de.cubeisland.cubeengine.core.event.EventManager;
import de.cubeisland.cubeengine.core.filesystem.FileManager;
import de.cubeisland.cubeengine.core.i18n.I18n;
import de.cubeisland.cubeengine.core.module.ModuleManager;
import de.cubeisland.cubeengine.core.permission.PermissionRegistration;
import de.cubeisland.cubeengine.core.storage.TableManager;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.user.UserManager;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;
import org.bukkit.Server;

/**
 *
 * @author Phillip Schichtel
 */
public interface Core
{
    public static final int REVISION = 2;
    
    /**
     * The method returns the Server
     * 
     * @return the server instance 
     */
    public Server getServer();
    
    /**
     * The method returns the database
     *
     * @return the database instance
     */
    public Database getDB();

    /**
     * The method returns the permission registration
     *
     * @return an instance of a permission registration
     */
    public PermissionRegistration getPermissionRegistration();

    /**
     * The method returns the event manager
     *
     * @return the instance of the event manager
     */
    public EventManager getEventManager();

    /**
     * This method returns the user manager
     *
     * @return the instance of the user manager
     */
    public UserManager getUserManager();

    /**
     * This method returns the file manager
     *
     * @return the instance of the file manager
     */
    public FileManager getFileManager();

    /**
     * This method returns the engine logger
     *
     * @return the engine logger
     */
    public Logger getCoreLogger();

    /**
     * This method returns the module manager
     *
     * @return the instance of the module manager
     */
    public ModuleManager getModuleManager();

    /**
     * This method returns the internationalization API
     *
     * @return the I18n API
     */
    public I18n getI18n();

    /**
     * This method returns the engine configuration
     *
     * @return the engine configuration
     */
    public CoreConfiguration getConfiguration();

    /**
     * This method returns the command manager
     *
     * @return the instance of the command manager
     */
    public CommandManager getCommandManager();
    
    /**
     * This method returns the Worker/ExecutorService
     * 
     * @return the ExecutorService
     */
    public ScheduledExecutorService getExecutor();
    
    /**
     * This method returns the DebugMode
     * 
     * @return the debugMode
     */
    public boolean isDebug();

    /**
     * This method returns the TableManager
     * 
     * @return the TableManager
     */
    public TableManager getTableManger();
}