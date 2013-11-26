package de.cubeisland.engine.reputation.modifiers;

import de.cubeisland.engine.core.user.User;

public class VoteModifier implements Modifier
{
    @Override
    public int getChange(User user)
    {
        return 5;
    }
}
