package net.unit8.kysymys.user.application.impl;

import net.unit8.kysymys.user.application.GetFollowersPort;
import net.unit8.kysymys.user.application.ListFollowersUseCase;
import net.unit8.kysymys.user.domain.User;
import net.unit8.kysymys.user.domain.UserId;

import java.util.List;

public class ListFollowersUseCaseImpl implements ListFollowersUseCase {
    private final GetFollowersPort getFollowersPort;

    public ListFollowersUseCaseImpl(GetFollowersPort getFollowersPort) {
        this.getFollowersPort = getFollowersPort;
    }

    @Override
    public List<User> handle(String userId) {
        return getFollowersPort.listFollowers(UserId.of(userId));
    }
}
