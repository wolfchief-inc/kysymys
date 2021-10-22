package net.unit8.kysymys.user.application;

import net.unit8.kysymys.user.domain.User;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ListFollowersUseCase {
    Page<User> handle(ListFollowersQuery query);
}
