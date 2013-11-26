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
package de.cubeisland.engine.reputation;

import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.reputation.storage.ReputationModel;
import org.jooq.DSLContext;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.reputation.storage.ReputationTable.TABLE_REPUTATION;

public class Reputation extends Module
{
    private ReputationConfig config;
    private DSLContext dsl;
    
    @Override
    public void onEnable()
    {
        this.config = this.loadConfig(ReputationConfig.class);
        this.dsl = this.getCore().getDB().getDSL();
    }

    public void modifyReputation(User user, int amount)
    {
        ReputationModel model = this.dsl.selectFrom(TABLE_REPUTATION)
                                                  .where(TABLE_REPUTATION.USER_ID.eq(UInteger.valueOf(user.getId())))
                                                  .fetchOne();
        if (model == null)
        {
            model = new ReputationModel();
        }
        model.setValue(model.getValue() + amount);
        model.update();
    }
}
