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
package de.cubeisland.engine.test.commands;

import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandResult;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.result.AsyncResult;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.time.Duration;

public class TestCommands
{

    @Command(desc = "Time-parsing")
    public void parsetime(CommandContext context)
    {
        LinkedList<String> list = new LinkedList<>();
        int i = 0;
        while (context.hasArg(i))
        {
            list.add(context.getString(i));
            i++;
        }
        Duration dura = new Duration(list.toArray(new String[0]));
        context.sendMessage("ms: " + dura.toTimeUnit(TimeUnit.MILLISECONDS));
        context.sendMessage(dura.format());
    }

    @Command(desc = "A command that tests async execution.")
    public CommandResult asyncCommand(CommandContext context)
    {
        context.sendMessage("Async GO!");
        return new AsyncResult() {
            @Override
            public void asyncMain(CommandContext sender)
            {
                try
                {
                    Thread.sleep(1000 * 5L);
                }
                catch (InterruptedException e)
                {}
                sender.sendMessage("Delayed!");
                try
                {
                    Thread.sleep(1000 * 5L);
                }
                catch (InterruptedException e)
                {}
            }

            @Override
            public void onFinish(CommandContext context)
            {
                context.sendMessage("Finished!");
            }
        };
    }

    @Command(desc = "This command prints out the args he gets", flags = @Flag(name = "a"), params = @Param(names = "param"))
    public void testArgs(ParameterizedContext context)
    {
        context.sendMessage("Arg dump:");
        context.sendMessage(" ");

        for (String arg : context.getArgs())
        {
            context.sendMessage("Arg: '" + arg + "'");
        }

        for (String flag : context.getFlags())
        {
            context.sendMessage("Flag: -" + flag);
        }

        for (Entry<String, Object> entry : context.getParams().entrySet())
        {
            context.sendMessage("Param: " + entry.getKey() + " => '" + entry.getValue().toString() + "'");
        }
    }

    private static final int MAX_CHAT_LINES = 100;

    @Command(names = {
        "cls", "clearscreen"
    }, desc = "Clears the chat")
    public void clearscreen(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            for (int i = 0; i < MAX_CHAT_LINES; ++i)
            {
                context.sendMessage(" ");
            }
        }
        else
        {
            context.sendMessage("&cYou better don't do this.");
        }
    }
}
