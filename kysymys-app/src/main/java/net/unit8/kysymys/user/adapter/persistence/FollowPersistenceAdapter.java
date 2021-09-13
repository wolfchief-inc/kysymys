package net.unit8.kysymys.user.adapter.persistence;

import net.unit8.kysymys.user.application.*;
import net.unit8.kysymys.user.domain.User;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
class FollowPersistenceAdapter implements IsFollowerPort, GetFollowersPort, AddFollowerPort, RemoveFollowerPort {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public FollowPersistenceAdapter(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public List<User> listFollowers(UserId userId) {
        return userRepository.findAllFollowers(userId.getValue())
                .stream().map(userMapper::entityToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isFollower(UserId followerId, UserId followeeId) {
        return userRepository.isFollower(followerId.getValue(), followeeId.getValue());
    }

    @Override
    public void follow(UserId followerId, UserId followeeId) {
        UserJpaEntity follower = userRepository.findById(followerId.getValue())
                .orElseThrow(() -> new UserNotFoundException(followerId.getValue()));
        UserJpaEntity followee = userRepository.findById(followeeId.getValue())
                .orElseThrow(() -> new UserNotFoundException(followeeId.getValue()));

        follower.getFollowers().add(followee);
        followee.getFollowers().add(follower);
    }

    @Override
    public void unfollow(UserId followerId, UserId followeeId) {
        UserJpaEntity follower = userRepository.findById(followerId.getValue())
                .orElseThrow(() -> new UserNotFoundException(followerId.getValue()));
        UserJpaEntity followee = userRepository.findById(followeeId.getValue())
                .orElseThrow(() -> new UserNotFoundException(followeeId.getValue()));

        follower.getFollowers().remove(followee);
        followee.getFollowers().remove(follower);
    }
}
