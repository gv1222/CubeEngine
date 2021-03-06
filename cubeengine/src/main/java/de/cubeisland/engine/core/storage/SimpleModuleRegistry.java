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
package de.cubeisland.engine.core.storage;

import de.cubeisland.engine.core.module.Module;

public class SimpleModuleRegistry implements ModuleRegistry
{
    private final Module module;
    private final Registry registry;

    public SimpleModuleRegistry(Module module, Registry registry)
    {
        this.module = module;
        this.registry = registry;
    }

    @Override
    public String get(String key)
    {
        return this.registry.getValue(key, module);
    }

    @Override
    public void set(String key, String value)
    {
        this.registry.merge(module, key, value);
    }

    @Override
    public String remove(String key)
    {
        return this.registry.delete(module, key);
    }

    @Override
    public void clear()
    {
        this.registry.clear(module);
    }
}
