package de.cubeisland.cubeengine.social.sites.facebook;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
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

    public String publishMessage(String message, String object)
    {
        return null; // TODO
    }

    public String publishMessage(String message)
    {
        return this.publishMessage(message, "me");
    }

    public String likeObject(String object)
    {
        return null; // TODO
    }

    public User getUserInfo()
    {
        return null; // TODO
    }

    public String getAuthToken()
    {
        return authToken;
    }
}
