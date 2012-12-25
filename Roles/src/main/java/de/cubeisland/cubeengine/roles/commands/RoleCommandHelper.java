package de.cubeisland.cubeengine.roles.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.*;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.storage.world.WorldManager;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.role.RoleManager;
import de.cubeisland.cubeengine.roles.role.config.RoleProvider;
import org.bukkit.World;

public abstract class RoleCommandHelper extends ContainerCommand
{
    protected RoleManager manager;
    protected Roles module;
    protected WorldManager worldManager;

    public RoleCommandHelper(Roles module)
    {
        super(module, "role", "Manage roles.");//TODO alias manrole
        this.manager = module.getManager();
        this.module = module;
        this.worldManager = module.getCore().getWorldManager();
    }

    protected World getWorld(CommandContext context)
    {

        User sender = context.getSenderAsUser();
        World world;
        if (!context.hasNamed("in"))
        {
            if (sender == null)
            {
                if (ModuleManagementCommands.curWorldIdOfConsole == null)
                {
                    invalidUsage(context, "roles", "&ePlease provide a world.\n&aYou can define a world with &6/roles admin defaultworld <world>");
                }
                world = this.worldManager.getWorld(ModuleManagementCommands.curWorldIdOfConsole);
            }
            else
            {
                world = this.worldManager.getWorld((Long) sender.getAttribute(this.module, "curWorldId"));
                if (world == null)
                {
                    world = sender.getWorld();
                }
            }
        }
        else
        {
            world = context.getNamed("in", World.class);
            if (world == null)
            {
                paramNotFound(context, "roles", "&cWorld %s not found!", context.getString("in"));
            }
        }
        return world;
    }

    protected RoleProvider getProvider(World world)
    {
        return this.manager.getProvider(this.worldManager.getWorldId(world));
    }
}