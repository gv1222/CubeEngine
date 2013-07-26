package de.cubeisland.engine.social.sites.facebook;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;

import de.cubeisland.engine.social.SocialConfig;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.user.User;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.FacebookApi;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

public class FacebookManager
{
    private final String APP_KEY;
    private final String APP_SECRET;
    private final String CALLBACK;
    private final Map<String, OAuthService> services;
    private final Map<String, FacebookUser> users; // @Quick_Wango you need to put the codes into here. new FacebookUser(service.getAccesToken(null, new Verifier("the code"))
    private final Map<Location, String> posts; //This should be saved to the database
    private final SocialConfig config;

    public FacebookManager(SocialConfig config)
    {
        this.APP_KEY = config.facebookAppKey;
        this.APP_SECRET = config.facebookAppSecret;
        this.CALLBACK = config.facebookCallbackURL;

        this.services = new HashMap<String, OAuthService>();
        this.config = config;
        this.users = new HashMap<String, FacebookUser>();
        this.posts = new HashMap<Location, String>();
    }

    public boolean initialize()
    {
        try
        {
            // TODO Listen for callbacks
            //Validate APP_KEY and APP_SECRET
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    public boolean hasUser(User user)
    {
        return users.containsKey(user.getName());
    }

    public FacebookUser getUser(User user)
    {
        return users.get(user.getName());
    }

    public String getAuthURL(User user)
    {
        services.put(user.getName(), new ServiceBuilder()
            .provider(FacebookApi.class)
            .apiKey(APP_KEY)
            .apiSecret(APP_SECRET)
            .callback(CALLBACK)
            .build());
        return services.get(user.getName()).getAuthorizationUrl(null) + "&state=" + user.getName();
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

    public void initializeUser(User user, String code)
    {
        CubeEngine.getLog().info("Code: {}", code);
        Verifier verifier = new Verifier(code);
        Token token = services.get(user.getName()).getAccessToken(null, verifier);
        CubeEngine.getLog().info("AuthToken: {}", token.getToken());
        users.put(user.getName(), new FacebookUser(token.getToken()));
    }
}
