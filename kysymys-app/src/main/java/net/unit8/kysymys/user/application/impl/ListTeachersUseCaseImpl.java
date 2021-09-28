package net.unit8.kysymys.user.application.impl;

import net.unit8.kysymys.stereotype.UseCase;
import net.unit8.kysymys.user.application.GetUsersPort;
import net.unit8.kysymys.user.application.ListTeacherQuery;
import net.unit8.kysymys.user.application.ListTeachersUseCase;
import net.unit8.kysymys.user.domain.Roles;
import net.unit8.kysymys.user.domain.User;
import org.springframework.data.domain.Page;

import java.util.Set;

@UseCase
public class ListTeachersUseCaseImpl implements ListTeachersUseCase {
    private final GetUsersPort getUsersPort;

    public ListTeachersUseCaseImpl(GetUsersPort getUsersPort) {
        this.getUsersPort = getUsersPort;
    }

    @Override
    public Page<User> handle(ListTeacherQuery query) {
        return getUsersPort.list(query.getQuery(), Roles.of(Set.of("TEACHER")), query.getPage());
    }
}
