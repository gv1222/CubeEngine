package de.cubeisland.engine.reputation.privilige;

import de.cubeisland.engine.core.user.User;

public interface Privilege
{
    void give(User user);
    void remove(User user);
}
