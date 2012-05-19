package de.cubeisland.cubeengine.core.modules;

import org.bukkit.plugin.Plugin;

/**
 *
 * @author Phillip Schichtel
 */
public interface CubeModule extends Plugin
{
    /**
     * Returns the name of this module
     *
     * @return the module name
     */
    public String getModuleName();
}
