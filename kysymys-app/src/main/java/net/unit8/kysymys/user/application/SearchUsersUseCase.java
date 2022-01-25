package net.unit8.kysymys.user.application;

import lombok.Value;
import net.unit8.kysymys.user.domain.User;
import org.springframework.data.domain.Page;

public interface SearchUsersUseCase {
    Page<User> handle(SearchUsersQuery query);

    @Value
    class SearchUsersQuery {
        String query;
        int page;
    }
}
