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
package de.cubeisland.engine.test.tests.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import de.cubeisland.engine.core.bukkit.PlayerLanguageReceivedEvent;
import de.cubeisland.engine.test.tests.Test;

public class ListenerTest extends Test
{
    private final de.cubeisland.engine.test.Test module;

    public ListenerTest(de.cubeisland.engine.test.Test module)
    {
        this.module = module;
    }

    @Override
    public void onEnable()
    {
        module.getCore().getEventManager().registerListener(module, new TestListener(module));
        module.getCore().getEventManager().registerListener(module, new Listener()
        {
            @EventHandler
            public void onLanguageReceived(PlayerLanguageReceivedEvent event)
            {
                module.getLog().debug("Player: {} Lang: {}", event.getPlayer().getName(), event.getLanguage());
            }
        });
        this.setSuccess(true);
    }

}
