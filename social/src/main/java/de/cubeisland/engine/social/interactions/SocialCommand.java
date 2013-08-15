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
package de.cubeisland.engine.social.interactions;

import de.cubeisland.engine.social.Social;

import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;

public class SocialCommand
{
    private final Social module;

    public SocialCommand(Social module)
    {
        this.module = module;
    }

    @Command(names = {
        "facebook", "fb"
    }, desc = "Facebook", params = {
        @Param(names = {
                                "User", "u"
        }, type = User.class),
            @Param(names = {
                "Code", "c"
            }, type = String.class)
    })
    public void facebook(ParameterizedContext context)
    {
        if (!context.isSender(User.class) && !context.hasParam("User"))
        {
            context.sendTranslated("You have to include a player to log in");
            return;
        }

        User user;
        if (context.hasParam("User"))
        {
            user = context.getParam("User");
        }
        else
        {
            user = (User)context.getSender();
        }

        if (context.hasParam("Code"))
        {
            String verifyCode = context.getString("Code");
            module.getFacebookManager().initializeUser(user, verifyCode);
            return;
        }

        context.sendTranslated("Here is your auth address: %s", module.getFacebookManager().getAuthURL(user));

        // @Quick_Wango This is where you need to get the response
    }
}
