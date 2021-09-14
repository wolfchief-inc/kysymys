package net.unit8.kysymys.user.application;

import net.unit8.kysymys.user.domain.User;

import java.util.Set;

public interface ListUsersUseCase {
    Set<User> handle(Set<String> userIds);
}
