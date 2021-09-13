package net.unit8.kysymys.user.application;

import net.unit8.kysymys.user.domain.UserId;

public interface AddFollowerPort {
    void follow(UserId followerId, UserId followeeId);
}
