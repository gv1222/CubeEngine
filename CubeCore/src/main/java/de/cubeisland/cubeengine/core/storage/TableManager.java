package de.cubeisland.cubeengine.core.storage;

import de.cubeisland.cubeengine.core.Core;
import gnu.trove.map.hash.THashMap;

/**
 *
 * @author Anselm Brehme
 */
public class TableManager extends BasicStorage<Table>
{
    private THashMap<String, Table> tables = new THashMap<String, Table>();

    public TableManager(final Core core)
    {
        super(core.getDB(), Table.class, Core.REVISION);
        tableManager = this;
        this.initialize();
        for (Table t : this.getAll())
        {
            tables.put(t.table, t);
        }
    }

    public void registerTable(String table, Integer revision)
    {
        Table t = this.tables.get(table);
        if (t != null)
        {
            t.revision = revision;
            this.update(t);
            return; // Table got updated!
        }
        this.store(new Table(table, revision));
    }

    /**
     * Gets the Revision of a table in database
     *
     * @param table the table
     * @return The revision of given table OR -1 if table not registered yet
     */
    public int getRevision(String table)
    {
        Table t = this.tables.get(table);
        if (t != null)
        {
            return t.revision;
        }
        return -1;
    }
}