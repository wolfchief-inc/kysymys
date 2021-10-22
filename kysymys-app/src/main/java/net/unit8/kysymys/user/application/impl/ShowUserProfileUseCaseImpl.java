package net.unit8.kysymys.user.application.impl;

import net.unit8.kysymys.stereotype.UseCase;
import net.unit8.kysymys.user.application.*;
import net.unit8.kysymys.user.domain.FollowStatus;
import net.unit8.kysymys.user.domain.User;
import net.unit8.kysymys.user.domain.UserId;
import net.unit8.kysymys.user.domain.UserProfileByOther;
import org.springframework.data.domain.Page;

import java.util.List;

@UseCase
class ShowUserProfileUseCaseImpl implements ShowUserProfileUseCase {
    private final LoadUserPort loadUserPort;
    private final GetFollowersPort getFollowersPort;
    private final HasAlreadyOfferedPort hasAlreadyOfferedPort;

    ShowUserProfileUseCaseImpl(LoadUserPort loadUserPort, GetFollowersPort getFollowersPort, HasAlreadyOfferedPort hasAlreadyOfferedPort) {
        this.loadUserPort = loadUserPort;
        this.getFollowersPort = getFollowersPort;
        this.hasAlreadyOfferedPort = hasAlreadyOfferedPort;
    }

    private FollowStatus followStatus(boolean hasAlreadyOffered, boolean hasAlreadyFollowed) {
        if (hasAlreadyFollowed) return FollowStatus.FOLLOWING;
        if (hasAlreadyOffered) return FollowStatus.HAS_ALREADY_OFFERED;
        return FollowStatus.UNFOLLOWING;
    }

    @Override
    public UserProfileByOther handle(ShowUserProfileQuery query) {
        UserId userId = UserId.of(query.getTargetUserId());
        UserId viewerUserId = UserId.of(query.getViewerUserId());

        User user = loadUserPort.load(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.getValue()));

        Page<User> followers = getFollowersPort.listFollowers(userId, 0, Integer.MAX_VALUE);
        FollowStatus followStatus = userId.equals(viewerUserId) ? null :
                followStatus(hasAlreadyOfferedPort.hasAlreadyOffered(viewerUserId, userId),
                followers.stream().anyMatch(follower -> follower.getId().equals(viewerUserId)));

        return new UserProfileByOther(
                user,
                followers.getContent(),
                followStatus
        );
    }
}
