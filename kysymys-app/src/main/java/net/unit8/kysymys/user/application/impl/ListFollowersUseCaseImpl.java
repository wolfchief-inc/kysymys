package net.unit8.kysymys.user.application.impl;

import net.unit8.kysymys.stereotype.UseCase;
import net.unit8.kysymys.user.application.GetFollowersPort;
import net.unit8.kysymys.user.application.ListFollowersQuery;
import net.unit8.kysymys.user.application.ListFollowersUseCase;
import net.unit8.kysymys.user.domain.User;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.data.domain.Page;

import java.util.List;

@UseCase
class ListFollowersUseCaseImpl implements ListFollowersUseCase {
    private final GetFollowersPort getFollowersPort;

    public ListFollowersUseCaseImpl(GetFollowersPort getFollowersPort) {
        this.getFollowersPort = getFollowersPort;
    }

    @Override
    public Page<User> handle(ListFollowersQuery query) {
        return getFollowersPort.listFollowers(UserId.of(query.getUserId()), query.getPage(), query.getSize());
    }
}
