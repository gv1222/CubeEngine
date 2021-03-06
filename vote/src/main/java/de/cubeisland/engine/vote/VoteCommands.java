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
package de.cubeisland.engine.vote;

import java.util.concurrent.TimeUnit;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.time.Duration;
import de.cubeisland.engine.vote.storage.VoteModel;

import static de.cubeisland.engine.vote.storage.TableVote.TABLE_VOTE;

public class VoteCommands
{
    private Vote module;

    public VoteCommands(Vote module)
    {
        this.module = module;
    }

    @Command(desc = "shows your current vote situation")
    public void vote(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            VoteModel voteModel = module.dsl.selectFrom(TABLE_VOTE)
                          .where(TABLE_VOTE.USERID.eq(((User)context.getSender()).getEntity().getKey()))
                        .fetchOne();
            if (voteModel == null)
            {
                context.sendTranslated("&eSorry but you do not have any registered votes on this server!");
            }
            else
            {
                context.sendTranslated("&aYou current vote-count is &6%d", voteModel.getVoteamount().intValue());
                if (System.currentTimeMillis() - voteModel.getLastvote().getTime() >= module.getConfig().voteBonusTime.toMillis())
                {
                    context.sendTranslated("&eSadly you did not vote in the last &6%s&e so your vote-count will be reset to 1",
                                           module.getConfig().voteBonusTime.format("%www%ddd%hhh%mmm%sss"));
                }
                else if (System.currentTimeMillis() - voteModel.getLastvote().getTime() < TimeUnit.DAYS.toMillis(1))
                {
                    context.sendTranslated("&aYou voted &6%s&a ago so you will probably not be able to vote again already!",
                                          new Duration(System.currentTimeMillis() - voteModel.getLastvote().getTime()).format("%www%ddd%hhh%mmm%sss"));
                }
                else
                {
                    context.sendTranslated("&eVoting now will increase your consecutive votes and result in higher reward!");
                }
                if (!module.getConfig().voteUrl.isEmpty())
                {
                    context.sendTranslated("&aYou can vote here now: &6%s", module.getConfig().voteUrl);
                }
            }
            return;
        }
        context.sendTranslated("&eWell you wont get any rewards.");
        if (!module.getConfig().voteUrl.isEmpty())
        {
            context.sendTranslated("&eBut here go vote anyways: &6%s", module.getConfig().voteUrl);
        }
    }
}
