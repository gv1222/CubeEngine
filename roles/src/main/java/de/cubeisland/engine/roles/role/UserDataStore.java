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
package de.cubeisland.engine.roles.role;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.cubeisland.engine.roles.Roles;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import org.jooq.types.UInteger;

public class UserDataStore implements RawDataStore
{
    protected Set<String> roles;
    protected Map<String,Boolean> permissions;
    protected Map<String,String> metadata;

    protected final RolesAttachment attachment;
    protected final long worldID;

    public UserDataStore(RolesAttachment attachment, long worldID)
    {
        this.attachment = attachment;
        this.worldID = worldID;

        this.roles = new HashSet<>();
        this.permissions = new HashMap<>();
        this.metadata = new HashMap<>();
    }

    @Override
    public Map<String, Boolean> getRawPermissions()
    {
       return this.permissions;
    }

    @Override
    public Map<String, String> getRawMetadata()
    {
        return this.metadata;
    }

    @Override
    public Set<String> getRawAssignedRoles()
    {
       return this.roles;
    }

    @Override
    public Set<Role> getAssignedRoles()
    {
        return Collections.unmodifiableSet(this.attachment.getResolvedData(this.worldID).assignedRoles);
    }

    @Override
    public String getName()
    {
        return this.attachment.getHolder().getName();
    }

    @Override
    public void setPermission(String perm, Boolean set)
    {
        if (set == null)
        {
            this.permissions.remove(perm);
        }
        else
        {
            this.permissions.put(perm,set);
        }
        this.makeDirty();
    }

    @Override
    public void setMetadata(String key, String value)
    {
        if (value == null)
        {
            this.metadata.remove(key);
        }
        else
        {
            this.metadata.put(key,value);
        }
        this.makeDirty();
    }

    @Override
    public boolean assignRole(Role role)
    {
        String roleName = role.getName();
        if (role.isGlobal())
        {
            roleName = "g:" + roleName;
        }
        if (this.roles.add(roleName))
        {
            this.makeDirty();
            return true;
        }
        return false;
    }

    @Override
    public boolean removeRole(Role role)
    {
        String roleName = role.getName();
        if (role.isGlobal())
        {
            roleName = "g:" + roleName;
        }
        if (this.roles.remove(roleName))
        {
            this.makeDirty();
            return true;
        }
        return false;
    }

    @Override
    public void clearPermissions()
    {
      this.permissions = new THashMap<>();
        this.makeDirty();
    }

    @Override
    public void clearMetadata()
    {
        this.metadata = new THashMap<>();
        this.makeDirty();
    }

    @Override
    public void clearAssignedRoles()
    {
        this.roles = new THashSet<>();
        this.makeDirty();
    }

    @Override
    public void setPermissions(Map<String, Boolean> perms)
    {
       this.permissions = new THashMap<>(perms);
        this.makeDirty();
    }

    @Override
    public void setMetadata(Map<String, String> metadata)
    {
        this.metadata = new THashMap<>(metadata);
        this.makeDirty();
    }

    @Override
    public void setAssignedRoles(Set<Role> roles)
    {
        this.clearAssignedRoles();
        for (Role role : roles)
        {
            this.roles.add(role.getName());
        }
        this.makeDirty();
    }

    @Override
    public long getWorldID()
    {
        return this.worldID;
    }

    public UInteger getUserID()
    {
        return this.attachment.getHolder().getEntity().getKey();
    }

    @Override
    public Map<String, Boolean> getAllRawPermissions()
    {
        return this.getRawPermissions();
    }

    @Override
    public Map<String, String> getAllRawMetadata()
    {
       return this.getRawMetadata();
    }

    protected void makeDirty()
    {
        this.attachment.makeDirty(worldID);
    }

    public long getMainWorldID()
    {
        return ((Roles)this.attachment.getModule()).getRolesManager().getProvider(worldID).getMainWorldId();
    }
}
