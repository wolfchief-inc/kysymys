package net.unit8.kysymys.user.application;

import net.unit8.kysymys.user.domain.UserId;

public interface RemoveFollowerPort {
    void unfollow(UserId followerId, UserId followeeId);
}
