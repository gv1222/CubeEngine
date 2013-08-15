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
package de.cubeisland.engine.signmarket;

import java.util.concurrent.TimeUnit;

import de.cubeisland.engine.core.config.Configuration;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.core.util.Profiler;
import de.cubeisland.engine.signmarket.storage.TableSignBlock;
import de.cubeisland.engine.signmarket.storage.TableSignItem;

public class Signmarket extends Module
{
    private MarketSignFactory marketSignFactory;
    private SignMarketConfig config;
    private EditModeListener editModeListener;

    @Override
    public void onEnable()
    {
        Profiler.startProfiling("marketSignEnable");
        Database db = this.getCore().getDB();
        db.registerTable(TableSignItem.initTable(db)); // Init Item-table first!!!
        db.registerTable(TableSignBlock.initTable(db));
        this.config = Configuration.load(SignMarketConfig.class, this);
        this.getLog().trace("{} ms - MarketSignFactory", Profiler.getCurrentDelta("marketSignEnable", TimeUnit.MILLISECONDS));
        this.marketSignFactory = new MarketSignFactory(this);
        this.getLog().trace("{} ms - MarketSignFactory-loadAllSigns", Profiler.getCurrentDelta("marketSignEnable", TimeUnit.MILLISECONDS));
        this.marketSignFactory.loadInAllSigns();
        this.getLog().trace("{} ms - EditModeListener", Profiler.getCurrentDelta("marketSignEnable", TimeUnit.MILLISECONDS));
        this.editModeListener = new EditModeListener(this);
        this.getLog().trace("{} ms - MarketSignListener", Profiler.getCurrentDelta("marketSignEnable", TimeUnit.MILLISECONDS));
        this.getCore().getEventManager().registerListener(this, new MarketSignListener(this));
        this.getLog().trace("{} ms - Perms", Profiler.getCurrentDelta("marketSignEnable", TimeUnit.MILLISECONDS));
        new MarketSignPerm(this);
        this.getLog().trace("{} ms - Command", Profiler.getCurrentDelta("marketSignEnable", TimeUnit.MILLISECONDS));
        this.getCore().getCommandManager().registerCommand(new SignMarketCommands(this));
        this.getLog().trace("{} ms - done", Profiler.endProfiling("marketSignEnable", TimeUnit.MILLISECONDS));
    }

    public MarketSignFactory getMarketSignFactory()
    {
        return this.marketSignFactory;
    }

    public SignMarketConfig getConfig()
    {
        return this.config;
    }

    public EditModeListener getEditModeListener()
    {
        return this.editModeListener;
    }
}
