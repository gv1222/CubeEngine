package de.cubeisland.cubeengine.core.storage.database.mysql;

import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.AlterTableBuilder;

/**
 * MYSQLQueryBuilder for altering tables.
 */
public class MySQLAlterTableBuilder extends
    MySQLComponentBuilder<AlterTableBuilder> implements AlterTableBuilder
{
    public MySQLAlterTableBuilder(MySQLQueryBuilder parent)
    {
        super(parent);
    }

    @Override
    public AlterTableBuilder alterTable(String table)
    {
        this.query = new StringBuilder("ALTER TABLE ").append(this.database.prepareTableName(table)).append(' ');
        return this;
    }

    @Override
    public AlterTableBuilder add(String field, AttrType type)
    {
        this.query.append("ADD ").append(this.database.prepareFieldName(field)).append(" ").append(type.getType());
        return this;
    }

    @Override
    public AlterTableBuilder drop(String field)
    {
        this.query.append("DROP COLUMN ").append(this.database.prepareFieldName(field));
        return this;
    }

    @Override
    public AlterTableBuilder modify(String field, AttrType type)
    {
        this.query.append("ALTER COLUMN ").append(this.database.prepareFieldName(field)).append(" ").append(type.getType());
        return this;
    }

    @Override
    public AlterTableBuilder addUniques(String... fields)
    {
        this.query.append("ADD UNIQUE (").append(this.database.prepareFieldName(fields[0]));
        for (int i = 1; i < fields.length; ++i)
        {
            this.query.append(", ").append(this.database.prepareFieldName(fields[i]));
        }
        this.query.append(")");
        return this;
    }

    @Override
    public AlterTableBuilder defaultValue(String value)
    {
        this.defaultValue();
        this.query.append(value);
        return this;
    }

    @Override
    public AlterTableBuilder defaultValue()
    {
        this.query.append(" DEFAULT ");
        return this;
    }

    @Override
    public AlterTableBuilder addCheck()
    {
        throw new UnsupportedOperationException("Not supported yet."); //TODO
    }

    @Override
    public AlterTableBuilder setDefault(String field)
    {
        this.query.append(" MODIFY ").append(this.database.prepareFieldName(field)).append(" DEFAULT ? ");
        return this;
    }

    @Override
    public AlterTableBuilder addForeignKey(String field, String foreignTable, String foreignField)
    {
        this.query.append(" ADD FOREIGN KEY (").append(this.database.prepareFieldName(field))
            .append(") REFERENCES ").append(this.database.prepareTableName(foreignTable)).append(".")
            .append(this.database.prepareFieldName(foreignField));
        return this;
    }

    @Override
    public AlterTableBuilder setPrimary(String field)
    {
        this.query.append(" ADD PRIMARY (").append(this.database.prepareFieldName(field)).append(")");
        return this;
    }

    @Override
    public AlterTableBuilder dropUnique(String field)
    {
        this.query.append("DROP INDEX ").append(this.database.prepareFieldName(field));
        return this;
    }

    @Override
    public AlterTableBuilder dropPrimary()
    {
        this.query.append("DROP PRIMARY KEY");
        return this;
    }

    @Override
    public AlterTableBuilder dropCheck(String field)
    {
        this.query.append("DROP CHECK ").append(this.database.prepareFieldName(field));
        return this;
    }

    @Override
    public AlterTableBuilder dropDefault(String field)
    {
        this.query.append("MODIFY ").append(this.database.prepareFieldName(field)).append(" DROP DEFAULT");
        return this;
    }

    @Override
    public AlterTableBuilder dropIndex(String field)
    {
        this.query.append("DROP INDEX ").append(this.database.prepareFieldName(field));
        return this;
    }

    @Override
    public AlterTableBuilder dropForeignKey(String field)
    {
        this.query.append("DROP FOREIGN KEY ").append(this.database.prepareFieldName(field));
        return this;
    }
}