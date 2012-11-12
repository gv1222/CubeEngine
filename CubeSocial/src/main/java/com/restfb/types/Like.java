package com.restfb.types;

import com.restfb.Facebook;

/**
 * Represents the Like Graph API type.
 *
 * @since 1.6.11
 */
public class Like
{
    @Facebook
    private String id;

    public String getId()
    {
        return this.id;
    }
}
