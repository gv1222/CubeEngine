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
package de.cubeisland.engine.social.sites.facebook;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.exception.FacebookException;
import com.restfb.types.FacebookType;
import com.restfb.types.User;

public class FacebookUser
{
    private final String authToken;
    private final FacebookClient client;

    public FacebookUser(String authToken)
    {
        this.authToken = authToken;
        this.client = new DefaultFacebookClient(this.authToken);
    }

    public FacebookType publishMessage(String message, String object) throws FacebookException
    {
        return client.publish(object + "/feed", FacebookType.class, Parameter.with("message", message));
    }

    public FacebookType publishMessage(String message) throws FacebookException
    {
        return this.publishMessage(message, "me");
    }

    public Boolean likeObject(String object) throws FacebookException
    {
        return client.publish(object + "/likes", Boolean.class);
    }

    public User getUserInfo() throws FacebookException
    {
        return client.fetchObject("me", User.class);
    }

    public String getAuthToken()
    {
        return authToken;
    }
}
