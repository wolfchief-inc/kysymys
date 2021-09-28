package net.unit8.kysymys.user.application;

import net.unit8.kysymys.user.domain.Roles;
import net.unit8.kysymys.user.domain.User;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.data.domain.Page;

import java.util.Set;

public interface GetUsersPort {
    Page<User> list(String query, int page);
    Page<User> list(String query, Roles role, int page);
    Set<User> listByUserIds(Set<UserId> userIds);
}
