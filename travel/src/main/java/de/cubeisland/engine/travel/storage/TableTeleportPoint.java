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
package de.cubeisland.engine.travel.storage;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.core.storage.database.TableCreator;
import de.cubeisland.engine.core.storage.database.mysql.Keys;
import de.cubeisland.engine.core.storage.database.mysql.MySQLDatabaseConfiguration;
import de.cubeisland.engine.core.user.UserEntity;
import de.cubeisland.engine.core.util.Version;
import de.cubeisland.engine.core.world.WorldEntity;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.core.user.TableUser.TABLE_USER;
import static de.cubeisland.engine.core.world.TableWorld.TABLE_WORLD;

public class TableTeleportPoint extends TableImpl<TeleportPointModel> implements TableCreator<TeleportPointModel>
{
    public static TableTeleportPoint TABLE_TP_POINT;

    private TableTeleportPoint(String prefix)
    {
        super(prefix + "teleportpoints");
        IDENTITY = Keys.identity(this, this.KEY);
        PRIMARY_KEY = Keys.uniqueKey(this, this.KEY);
        UNIQUE_OWNER_NAME_TYPE = Keys.uniqueKey(this, this.OWNER, this.NAME, this.TYPE);
        FOREIGN_USER = Keys.foreignKey(TABLE_USER.PRIMARY_KEY, this, this.OWNER);
        FOREIGN_WORLD = Keys.foreignKey(TABLE_WORLD.PRIMARY_KEY, this, this.WORLD);
    }

    public static TableTeleportPoint initTable(Database database)
    {
        MySQLDatabaseConfiguration config = (MySQLDatabaseConfiguration)database.getDatabaseConfig();
        TABLE_TP_POINT = new TableTeleportPoint(config.tablePrefix);
        return TABLE_TP_POINT;
    }

    @Override
    public void createTable(Connection connection) throws SQLException
    {
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.getName()+ " (\n" +
                                        "`key` int(10) unsigned NOT NULL AUTO_INCREMENT,\n" +
                                        "`owner` int(10) unsigned NOT NULL,\n" +
                                        "`type` smallint(1) NOT NULL,\n" +
                                        "`visibility` smallint(1) NOT NULL,\n" +
                                        "`world` int(10) unsigned NOT NULL,\n" +
                                        "`x` double NOT NULL,\n" +
                                        "`y` double NOT NULL,\n" +
                                        "`z` double NOT NULL,\n" +
                                        "`yaw` float NOT NULL,\n" +
                                        "`pitch` float NOT NULL,\n" +
                                        "`name` varchar(32) NOT NULL,\n" +
                                        "`welcomemsg` longtext,\n" +
                                        "PRIMARY KEY (`key`),\n" +
                                        "UNIQUE KEY `owner` (`owner`,`name`,`type`),\n" +
                                        "FOREIGN KEY `f_world`(`world`) REFERENCES " + TABLE_WORLD.getName() + "(`key`) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                                        "FOREIGN KEY `f_user`(`owner`) REFERENCES " + TABLE_USER.getName() + "(`key`) ON UPDATE CASCADE ON DELETE CASCADE)\n" +
                                        "ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci\n" +
                                        "COMMENT='1.0.0'").execute();
    }

    private static final Version version = new Version(1);

    @Override
    public Version getTableVersion()
    {
        return version;
    }

    public final Identity<TeleportPointModel, UInteger> IDENTITY;
    public final UniqueKey<TeleportPointModel> PRIMARY_KEY;
    public final UniqueKey<TeleportPointModel> UNIQUE_OWNER_NAME_TYPE;
    public final ForeignKey<TeleportPointModel, UserEntity> FOREIGN_USER;
    public final ForeignKey<TeleportPointModel, WorldEntity> FOREIGN_WORLD;

    public final TableField<TeleportPointModel, UInteger> KEY = createField("key", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<TeleportPointModel, UInteger> OWNER = createField("owner", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<TeleportPointModel, Short> TYPE = createField("type", SQLDataType.SMALLINT, this);
    public final TableField<TeleportPointModel, Short> VISIBILITY = createField("visibility", SQLDataType.SMALLINT, this);
    public final TableField<TeleportPointModel, UInteger> WORLD = createField("world", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<TeleportPointModel, Double> X = createField("x", SQLDataType.DOUBLE, this);
    public final TableField<TeleportPointModel, Double> Y = createField("y", SQLDataType.DOUBLE, this);
    public final TableField<TeleportPointModel, Double> Z = createField("z", SQLDataType.DOUBLE, this);
    public final TableField<TeleportPointModel, Double> YAW = createField("yaw", SQLDataType.FLOAT, this);
    public final TableField<TeleportPointModel, Double> PITCH = createField("pitch", SQLDataType.FLOAT, this);
    public final TableField<TeleportPointModel, String> NAME = createField("name", SQLDataType.VARCHAR.length(32), this);
    public final TableField<TeleportPointModel, String> WELCOMEMSG = createField("welcomemsg", SQLDataType.CLOB, this);

    @Override
    public Identity<TeleportPointModel, UInteger> getIdentity()
    {
        return IDENTITY;
    }

    @Override
    public UniqueKey<TeleportPointModel> getPrimaryKey()
    {
        return PRIMARY_KEY;
    }

    @Override
    public List<UniqueKey<TeleportPointModel>> getKeys()
    {
        return Arrays.asList(PRIMARY_KEY, UNIQUE_OWNER_NAME_TYPE);
    }

    @Override
    public List<ForeignKey<TeleportPointModel, ?>> getReferences() {
        return Arrays.<ForeignKey<TeleportPointModel, ?>>asList(FOREIGN_USER, FOREIGN_WORLD);
    }

    @Override
    public Class<TeleportPointModel> getRecordType() {
        return TeleportPointModel.class;
    }
}
