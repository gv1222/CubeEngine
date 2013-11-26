package de.cubeisland.engine.reputation.modifiers;

import de.cubeisland.engine.core.user.User;

public interface Modifier
{
    int getChange(User user);
}
