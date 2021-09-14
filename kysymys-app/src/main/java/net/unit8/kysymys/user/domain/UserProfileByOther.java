package net.unit8.kysymys.user.domain;

import lombok.Value;

import java.util.List;
import java.util.Optional;

@Value
public class UserProfileByOther {
    User user;
    List<User> follower;
    FollowStatus followStatus;

    public Optional<FollowStatus> getFollowStatus() {
        return Optional.ofNullable(followStatus);
    }
}
