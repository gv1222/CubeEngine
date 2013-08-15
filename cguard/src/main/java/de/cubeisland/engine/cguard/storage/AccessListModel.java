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
package de.cubeisland.engine.cguard.storage;

import de.cubeisland.engine.core.user.User;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.cguard.storage.TableAccessList.TABLE_ACCESS_LIST;

public class AccessListModel extends UpdatableRecordImpl<AccessListModel> implements Record4<UInteger, UInteger, UInteger, Short>
{
    public AccessListModel()
    {
        super(TABLE_ACCESS_LIST);
    }

    public AccessListModel newAccess(GuardModel model, User modifyUser)
    {
        this.setGuardId(model.getId());
        this.setUserId(modifyUser.getEntity().getKey());
        this.setLevel(ACCESS_FULL);
        return this;
    }

    public boolean canIn()
    {
        return (this.getLevel() & ACCESS_PUT) == ACCESS_PUT;
    }

    public boolean canOut()
    {
        return (this.getLevel() & ACCESS_TAKE) == ACCESS_TAKE;
    }

    public static final short ACCESS_TAKE = 1 << 0; // put items in chest
    public static final short ACCESS_PUT = 1 << 1; // take items out of chest
    public static final short ACCESS_ADMIN = 1 << 2; // manage accesslist

    public static final short ACCESS_FULL = ACCESS_TAKE | ACCESS_PUT;
    public static final short ACCESS_ALL = ACCESS_FULL | ACCESS_ADMIN;

    public void setId(UInteger value) {
        setValue(0, value);
    }

    public UInteger getId() {
        return (UInteger) getValue(0);
    }

    public void setUserId(UInteger value) {
        setValue(1, value);
    }

    public UInteger getUserId() {
        return (UInteger) getValue(1);
    }

    public void setGuardId(UInteger value) {
        setValue(2, value);
    }

    public UInteger getGuardId() {
        return (UInteger) getValue(2);
    }

    public void setLevel(Short value) {
        setValue(3, value);
    }

    public Short getLevel() {
        return (Short) getValue(3);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<UInteger> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record4 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row4<UInteger, UInteger, UInteger, Short> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    @Override
    public Row4<UInteger, UInteger, UInteger, Short> valuesRow() {
        return (Row4) super.valuesRow();
    }

    @Override
    public Field<UInteger> field1() {
        return TABLE_ACCESS_LIST.ID;
    }

    @Override
    public Field<UInteger> field2() {
        return TABLE_ACCESS_LIST.USER_ID;
    }

    @Override
    public Field<UInteger> field3() {
        return TABLE_ACCESS_LIST.GUARD_ID;
    }

    @Override
    public Field<Short> field4() {
        return TABLE_ACCESS_LIST.LEVEL;
    }

    @Override
    public UInteger value1() {
        return getId();
    }

    @Override
    public UInteger value2() {
        return getUserId();
    }

    @Override
    public UInteger value3() {
        return getGuardId();
    }

    @Override
    public Short value4() {
        return getLevel();
    }
}
