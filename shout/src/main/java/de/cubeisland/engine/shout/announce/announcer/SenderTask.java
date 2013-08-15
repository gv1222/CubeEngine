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
package de.cubeisland.engine.shout.announce.announcer;

import java.util.concurrent.Callable;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.shout.announce.receiver.Receiver;

class SenderTask implements Callable<Void>
{
    private final String[] message;
    private final Receiver receiver;

    public SenderTask(String[] message, Receiver receiver)
    {
        this.message = message;
        this.receiver = receiver;
    }

    @Override
    public Void call() throws Exception
    {
        String[] newline = {""};
        receiver.sendMessage(newline);
        receiver.sendMessage(this.message);
        receiver.sendMessage(newline);
        CubeEngine.getLog().trace("Sent a message to {}", receiver.getName());
        return null;
    }
}