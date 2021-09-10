package net.unit8.kysymys.user.application.impl;

import net.unit8.kysymys.user.application.*;
import net.unit8.kysymys.user.domain.User;
import net.unit8.kysymys.user.domain.UserId;
import net.unit8.kysymys.user.domain.UserProfileByOther;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class ShowUserProfileUseCaseImpl implements ShowUserProfileUseCase {
    private final LoadUserPort loadUserPort;
    private final GetFollowersPort getFollowersPort;

    ShowUserProfileUseCaseImpl(LoadUserPort loadUserPort, GetFollowersPort getFollowersPort) {
        this.loadUserPort = loadUserPort;
        this.getFollowersPort = getFollowersPort;
    }

    @Override
    public UserProfileByOther handle(ShowUserProfileQuery query) {
        UserId userId = UserId.of(query.getTargetUserId());
        User user = loadUserPort.load(UserId.of(query.getTargetUserId()))
                .orElseThrow(() -> new UserNotFoundException(userId.getValue()));

        List<User> followers = getFollowersPort.listFollowers(userId);

        return new UserProfileByOther(
                user,
                followers,
                followers.stream().anyMatch(follower -> follower.getId().equals(query.getViewerUserId()))
        );
    }
}
