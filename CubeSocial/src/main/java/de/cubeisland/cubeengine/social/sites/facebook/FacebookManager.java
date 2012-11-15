package de.cubeisland.cubeengine.social.sites.facebook;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.exception.FacebookException;
import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class FacebookManager
{
    private static final String           APP_KEY    = ""; //
    private static final String           APP_SECRET = ""; // This is used for getting access tokens for users using OAuth
    private final Map<User, FacebookUser> users;
    private final Map<Location, String>   posts;          //This should be saved to the database
    private FacebookClient                publicClient;

    public FacebookManager()
    {
        this.users = new HashMap<User, FacebookUser>();
        this.posts = new HashMap<Location, String>();
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

    public boolean initializeUser(User user, String accessToken)
    {
        try
        {
            FacebookUser facebookUser = new FacebookUser(accessToken);
            if (facebookUser.getUserInfo() != null)
            {
                users.put(user, facebookUser);
                return true;
            }
            return false;
        }
        catch (FacebookException ex)
        {
            return false;
        }
    }

    public boolean hasPost(Location loc)
    {
        return posts.containsKey(loc);
    }

    public void addPosts(Location loc, String id)
    {
        this.posts.put(loc, id);
    }

    public String fetchPost(Location loc)
    {
        return this.posts.get(loc);
    }
}
