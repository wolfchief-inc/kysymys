package net.unit8.kysymys.user.domain;

import lombok.Value;

import java.util.List;

@Value
public class UserProfileByOther {
    User user;
    List<User> follower;
    FollowStatus followStatus;
}
