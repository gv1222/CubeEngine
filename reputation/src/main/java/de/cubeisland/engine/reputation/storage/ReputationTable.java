package de.cubeisland.engine.reputation.storage;

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
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.core.user.TableUser.TABLE_USER;

public class ReputationTable extends TableImpl<ReputationModel> implements TableCreator<ReputationModel>
{
    public static ReputationTable TABLE_REPUTATION;

    private ReputationTable(String prefix)
    {
        super(prefix + "reputation");
        IDENTITY = Keys.identity(this, this.USER_ID);
        PRIMARY_KEY = Keys.uniqueKey(this, this.USER_ID);
        FOREIGN_USER = Keys.foreignKey(TABLE_USER.PRIMARY_KEY, this, this.USER_ID);
    }

    public static ReputationTable initTable(Database database)
    {
        MySQLDatabaseConfiguration config = (MySQLDatabaseConfiguration)database.getDatabaseConfig();
        TABLE_REPUTATION = new ReputationTable(config.tablePrefix);
        return TABLE_REPUTATION;
    }

    @Override
    public void createTable(Connection connection) throws SQLException
    {
        connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.getName()+ " (\n" +
                                        "`user_id` int(10) unsigned NOT NULL,\n" +
                                        "`value` int(10) NOT NULL ,\n" +
                                        "PRIMARY KEY (`user_id`),\n" +
                                        "FOREIGN KEY `f_user`(`user_id`) REFERENCES " + TABLE_USER.getName() + "(`key`) ON UPDATE CASCADE ON DELETE CASCADE)\n" +
                                        "ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci\n" +
                                        "COMMENT='1.0.0'").execute();
    }

    private static final Version version = new Version(1);

    @Override
    public Version getTableVersion()
    {
        return version;
    }
    public final Identity<ReputationModel, UInteger> IDENTITY;
    public final UniqueKey<ReputationModel> PRIMARY_KEY;
    public final ForeignKey<ReputationModel, UserEntity> FOREIGN_USER;

    public final TableField<ReputationModel, UInteger> USER_ID = createField("user_id", SQLDataType.INTEGERUNSIGNED, this);
    public final TableField<ReputationModel, Integer> VALUE = createField("value", SQLDataType.INTEGER, this);

    @Override
    public Identity<ReputationModel, UInteger> getIdentity()
    {
        return IDENTITY;
    }

    @Override
    public UniqueKey<ReputationModel> getPrimaryKey()
    {
        return PRIMARY_KEY;
    }

    @Override
    public List<UniqueKey<ReputationModel>> getKeys()
    {
        return Arrays.asList(PRIMARY_KEY);
    }

    @Override
    public List<ForeignKey<ReputationModel, ?>> getReferences() {
        return Arrays.<ForeignKey<ReputationModel, ?>>asList(FOREIGN_USER);
    }

    @Override
    public Class<ReputationModel> getRecordType() {
        return ReputationModel.class;
    }
}
