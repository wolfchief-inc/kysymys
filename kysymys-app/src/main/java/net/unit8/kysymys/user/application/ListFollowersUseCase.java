package net.unit8.kysymys.user.application;

import lombok.Value;
import net.unit8.kysymys.user.domain.User;
import org.springframework.data.domain.Page;

public interface ListFollowersUseCase {
    Page<User> handle(ListFollowersQuery query);

    @Value
    class ListFollowersQuery {
        String userId;
        int page;
        int size;
    }
}
