package de.cubeisland.cubeengine.social;

import de.cubeisland.engine.core.config.Configuration;
import de.cubeisland.engine.core.config.annotations.Codec;
import de.cubeisland.engine.core.config.annotations.Comment;
import de.cubeisland.engine.core.config.annotations.Option;
import de.cubeisland.engine.core.config.annotations.Revision;

@Codec("yml")
@Revision(1)
public class SocialConfig extends Configuration
{
    @Option("facebook.key")
    @Comment("The application key for your facebook application")
    public String facebookAppKey = "";

    @Option("facebook.secret")
    @Comment("The application secret for your facebook application")
    public String facebookAppSecret = "";

    @Option("facebook.callback-uri")
    @Comment("This should be the same as the server address + /callback")
    public String facebookCallbackURL = "";

    @Override
    public String[] head()
    {
        return new String[] {
                "The global config for CubeSocial",
                "To get a Facebook app key and secret you have to register a facebook application",
                "this can be done here: https://developers.facebook.com/apps",
                ""
        };
    }
}
