package de.cubeisland.cubeengine.fly.database;

import de.cubeisland.cubeengine.core.storage.Model;
import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.DatabaseConstructor;
import de.cubeisland.cubeengine.core.storage.database.Entity;
import de.cubeisland.cubeengine.core.storage.database.Key;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.converter.ConversionException;
import de.cubeisland.cubeengine.core.util.converter.Convert;
import java.util.List;

/**
 *
 * @author Anselm Brehme
 */
@Entity(name="fly")
public class FlyModel implements Model<Integer>
{
    @Key
    @Attribute(type = AttrType.INT, unsigned = true)
    public final int key;
    @Attribute(type = AttrType.BOOLEAN)
    public boolean flying;

    @DatabaseConstructor
    public FlyModel(List<Object> args) throws ConversionException
    {
        this.key = Convert.fromObject(Integer.class, args.get(0));
        this.flying = (Boolean) args.get(1);
    }

    public FlyModel(User user)
    {
        this.key = user.getKey();
        this.flying = user.isFlying();
    }

    public Integer getKey()
    {
        return this.key;
    }

    public void setKey(Integer key)
    {
        throw new UnsupportedOperationException("Not supported.");
    }
}
