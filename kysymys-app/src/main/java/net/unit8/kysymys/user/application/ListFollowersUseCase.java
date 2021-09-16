package net.unit8.kysymys.user.application;

import net.unit8.kysymys.user.domain.User;

import java.util.List;

public interface ListFollowersUseCase {
    List<User> handle(String userId);
}
