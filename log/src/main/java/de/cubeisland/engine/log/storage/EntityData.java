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
package de.cubeisland.engine.log.storage;

import org.bukkit.entity.EntityType;

import com.fasterxml.jackson.databind.JsonNode;

public class EntityData
{
    public final EntityType entityType;

    public EntityData(EntityType entityType, JsonNode json)
    {
        this.entityType = entityType;
        //TODO get data from json
    }

    @Override
    public String toString()
    {
        return entityType.name();  //TODO pretty name CE-294 http://bugs.engine.cubeisland.de/browse/CE-294
    }
}
