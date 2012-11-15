package de.cubeisland.cubeengine.social.sites.facebook;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.exception.FacebookException;
import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.Location;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.FacebookApi;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import java.util.HashMap;
import java.util.Map;

public class FacebookManager
{
    private static final String           APP_KEY    = "462939110414739";
    private static final String           APP_SECRET = "c7ed1858459881a2988c45187c1e8963";
    private final Map<User, FacebookUser> users; // @Quick_Wango you need to put the codes into here. new FacebookUser(service.getAccesToken(null, new Verifier("the code"))
    private final Map<Location, String>   posts; //This should be saved to the database
    private FacebookClient                publicClient;
    private final OAuthService            service;

    public FacebookManager()
    {
        this.users = new HashMap<User, FacebookUser>();
        this.posts = new HashMap<Location, String>();
        this.publicClient = new DefaultFacebookClient();
        this.service = new ServiceBuilder()
            .provider(FacebookApi.class)
            .apiKey(APP_KEY)
            .apiSecret(APP_SECRET)
            .callback("http://www.google.com/")
            .build();
    }

    public boolean hasUser(User user)
    {
        return users.containsKey(user);
    }

    public FacebookUser getUser(User user)
    {
        return users.get(user);
    }

    public String getAuthURL()
    {
        return service.getAuthorizationUrl(null);
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
