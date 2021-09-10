package net.unit8.kysymys.user.application;

import net.unit8.kysymys.user.domain.User;
import net.unit8.kysymys.user.domain.UserId;

import java.util.List;

public interface GetFollowersPort {
    List<User> listFollowers(UserId userId);
}
