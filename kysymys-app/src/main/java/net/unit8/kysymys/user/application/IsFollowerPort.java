package net.unit8.kysymys.user.application;

import net.unit8.kysymys.user.domain.UserId;

public interface IsFollowerPort {
    boolean isFollower(UserId followerId, UserId followeeId);
}
