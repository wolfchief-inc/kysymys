package net.unit8.kysymys.user.application;

import net.unit8.kysymys.user.domain.User;
import net.unit8.kysymys.user.domain.UserId;

import java.util.Optional;

public interface LoadUserPort {
    Optional<User> load(UserId userId);
}
