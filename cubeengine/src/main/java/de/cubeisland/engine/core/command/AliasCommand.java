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
package de.cubeisland.engine.core.command;

import java.util.List;
import java.util.Stack;

import static de.cubeisland.engine.core.util.StringUtils.explode;

public final class AliasCommand extends CubeCommand
{
    private static final String[] NO_ADDITION = new String[0];
    private final CubeCommand target;
    private final String[] prefix;
    private final String[] suffix;

    public AliasCommand(CubeCommand target, String name, List<String> aliases, String prefix, String suffix)
    {
        super(target.getModule(), name, target.getDescription(), target.getUsage(), aliases, target.getContextFactory());
        this.target = target;
        this.prefix = (prefix == null || prefix.isEmpty() ? NO_ADDITION : explode(" ", prefix));
        this.suffix = (suffix == null || suffix.isEmpty() ? NO_ADDITION : explode(" ", suffix));
    }

    public CubeCommand getTarget()
    {
        return this.target;
    }

    public String[] getPrefix()
    {
        return this.prefix;
    }

    public String[] getSuffix()
    {
        return this.suffix;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args, String label, Stack<String> labels)
    {
        String[] prefix = this.getPrefix();
        String[] suffix = this.getSuffix();

        String[] newArgs = new String[prefix.length + args.length + suffix.length];
        System.arraycopy(prefix, 0, newArgs, 0, prefix.length);
        System.arraycopy(args, 0, newArgs, prefix.length, args.length);
        System.arraycopy(suffix, 0, newArgs, prefix.length + args.length, suffix.length);

        args = newArgs;
        return this.target.execute(sender, args, label, labels);
    }

    @Override
    public CommandResult run(CommandContext context) throws Exception
    {
        return this.target.run(context);
    }

    @Override
    public void help(HelpContext context) throws Exception
    {
        this.target.help(context);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args)
    {
        return this.target.tabComplete(sender, label, args);
    }
}
