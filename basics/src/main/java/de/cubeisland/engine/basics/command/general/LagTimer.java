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
package de.cubeisland.engine.basics.command.general;

import java.util.LinkedList;

import de.cubeisland.engine.core.util.Pair;
import de.cubeisland.engine.basics.Basics;

public class LagTimer implements Runnable
{
    private long lastTick = System.currentTimeMillis();
    private final LinkedList<Float> tpsHistory = new LinkedList<>();
    private final Basics module;

    private float lowestTPS = 20;
    private long lowestTPSTime = 0;
    private boolean reached20 = false;

    private long lastLowTps = 0;

    public LagTimer(Basics module) {
        this.module = module;
        module.getCore().getTaskManager().runTimer(module, this, 0, 20); //start timer
    }

    @Override
    public void run()
    {
        final long currentTick = System.currentTimeMillis();
        long timeSpent = (currentTick - lastTick) / 1000;
        if (timeSpent == 0)
        {
            timeSpent = 1;
        }
        if (tpsHistory.size() > 10)
        {
            tpsHistory.remove();
        }
        final float tps = 20f / timeSpent;
        if (tps <= 20)
        {
            if (tps == 20) this.reached20 = true;
            tpsHistory.add(tps);
            if (reached20 && tps < 20)
            {
                if (tps < lowestTPS)
                {
                    lowestTPS = tps;
                    this.lowestTPSTime = currentTick;
                }
                this.lastLowTps = currentTick;
            }
        }
        lastTick = currentTick;
    }

    public float getAverageTPS()
    {
        float ticks = 0;
        for (Float tps : tpsHistory)
        {
            if (tps != null)
            {
                ticks += tps;
            }
        }
        return ticks / tpsHistory.size();
    }

    public Pair<Long,Float> getLowestTPS()
    {
        return new Pair<>(this.lowestTPSTime,this.lowestTPS);
    }

    public long getLastLowTPS()
    {
        return this.lastLowTps;
    }

    public void resetLowestTPS()
    {
        this.lowestTPSTime = 0;
        this.lowestTPS = 20;
    }
}
