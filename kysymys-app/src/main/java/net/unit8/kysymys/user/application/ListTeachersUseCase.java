package net.unit8.kysymys.user.application;

import lombok.Value;
import net.unit8.kysymys.user.domain.User;
import org.springframework.data.domain.Page;

public interface ListTeachersUseCase {
    Page<User> handle(ListTeacherQuery query);

    @Value
    class ListTeacherQuery {
        String query;
        int page;
    }
}
