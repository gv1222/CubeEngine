package de.cubeisland.cubeengine.social.sites.facebook;

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
        return client.publish(object+"/likes",Boolean.class);
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
