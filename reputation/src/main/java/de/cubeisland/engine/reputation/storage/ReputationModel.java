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
package de.cubeisland.engine.reputation.storage;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.reputation.storage.ReputationTable.TABLE_REPUTATION;

public class ReputationModel extends UpdatableRecordImpl<ReputationModel> implements Record2<UInteger, Integer>
{
    public ReputationModel()
    {
        super(TABLE_REPUTATION);
    }

    public void setUserId(UInteger value) {
        setValue(0, value);
    }

    public UInteger getUserId() {
        return (UInteger) getValue(0);
    }

    public void setValue(Integer value) {
        setValue(1, value);
    }

    public Integer getValue() {
        return (Integer) getValue(1);
    }

    @Override
    public Record1<UInteger> key()
    {
        return (Record1)super.key();
    }

    @Override
    public Row2<UInteger, Integer> fieldsRow()
    {
        return (Row2<UInteger, Integer>)super.fieldsRow();
    }

    @Override
    public Row2<UInteger, Integer> valuesRow()
    {
        return (Row2<UInteger, Integer>)super.valuesRow();
    }

    @Override
    public Field<UInteger> field1()
    {
        return TABLE_REPUTATION.USER_ID;
    }

    @Override
    public Field<Integer> field2()
    {
        return TABLE_REPUTATION.VALUE;
    }

    @Override
    public UInteger value1()
    {
        return getUserId();
    }

    @Override
    public Integer value2()
    {
        return getValue();
    }
}
