package net.unit8.kysymys.user.application.impl;

import net.unit8.kysymys.stereotype.UseCase;
import net.unit8.kysymys.user.application.GetUsersPort;
import net.unit8.kysymys.user.application.SearchUsersUseCase;
import net.unit8.kysymys.user.domain.User;
import org.springframework.data.domain.Page;

@UseCase
class SearchUsersUseCaseImpl implements SearchUsersUseCase {
    private final GetUsersPort getUsersPort;

    SearchUsersUseCaseImpl(GetUsersPort getUsersPort) {
        this.getUsersPort = getUsersPort;
    }

    @Override
    public Page<User> handle(SearchUsersQuery query) {
        return getUsersPort.list(query.getQuery(), query.getPage());
    }
}
