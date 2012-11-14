package de.cubeisland.cubeengine.social.sites.facebook;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import de.cubeisland.cubeengine.core.user.User;

import java.util.HashMap;
import java.util.Map;

public class FacebookManager
{
    private static final String APP_KEY = "";   //
    private static final String APP_SECRET = "";// This is used for getting access tokens for users using OAuth
    private final Map<User, FacebookUser> users;
    private FacebookClient publicClient;

    public FacebookManager()
    {
        this.users = new HashMap<User, FacebookUser>();
        this.publicClient = new DefaultFacebookClient();
    }

    public boolean hasUser(User user)
    {
        return users.containsKey(user);
    }

    public FacebookUser getUser(User user)
    {
        return users.get(user);
    }

    public boolean initializeUser(User user)
    {
        return false; // TODO
    }
}
