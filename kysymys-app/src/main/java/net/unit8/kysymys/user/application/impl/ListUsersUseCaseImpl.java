package net.unit8.kysymys.user.application.impl;

import net.unit8.kysymys.stereotype.UseCase;
import net.unit8.kysymys.user.application.GetUsersPort;
import net.unit8.kysymys.user.application.ListUsersUseCase;
import net.unit8.kysymys.user.domain.User;
import net.unit8.kysymys.user.domain.UserId;

import java.util.Set;
import java.util.stream.Collectors;

@UseCase
class ListUsersUseCaseImpl implements ListUsersUseCase {
    private final GetUsersPort getUsersPort;

    public ListUsersUseCaseImpl(GetUsersPort getUsersPort) {
        this.getUsersPort = getUsersPort;
    }

    @Override
    public Set<User> handle(Set<String> userIds) {
        return getUsersPort.listByUserIds(userIds.stream().map(UserId::of).collect(Collectors.toSet()));
    }
}
