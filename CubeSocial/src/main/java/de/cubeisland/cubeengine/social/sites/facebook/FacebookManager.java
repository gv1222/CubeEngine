package de.cubeisland.cubeengine.social.sites.facebook;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.exception.FacebookException;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.social.SocialConfig;
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
    private final String                  APP_KEY;
    private final String                  APP_SECRET;
    private final Map<User, FacebookUser> users;     // @Quick_Wango you need to put the codes into here. new FacebookUser(service.getAccesToken(null, new Verifier("the code"))
    private final Map<Location, String>   posts;     //This should be saved to the database
    private final SocialConfig            config;
    private OAuthService                  service;

    public FacebookManager(SocialConfig config)
    {
        this.APP_KEY = config.facebookAppKey;
        this.APP_SECRET = config.facebookAppSecret;

        this.config = config;
        this.users = new HashMap<User, FacebookUser>();
        this.posts = new HashMap<Location, String>();
    }

    public boolean initialize()
    {
        // TODO Listen for callbacks

        this.service = new ServiceBuilder()
                .provider(FacebookApi.class)
                .apiKey(APP_KEY)
                .apiSecret(APP_SECRET)
                .callback(this.config.facebookCallbackURL + ":" + this.config.facebookCallbackPort)
                .build();

        //Validate APP_KEY and APP_SECRET
        return true;
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
