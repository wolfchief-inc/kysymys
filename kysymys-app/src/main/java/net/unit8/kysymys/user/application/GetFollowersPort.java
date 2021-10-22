package net.unit8.kysymys.user.application;

import net.unit8.kysymys.user.domain.User;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.data.domain.Page;

import java.util.List;

public interface GetFollowersPort {
    Page<User> listFollowers(UserId userId, int page, int size);
}
