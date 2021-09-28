package net.unit8.kysymys.user.application;

import net.unit8.kysymys.user.domain.User;
import org.springframework.data.domain.Page;

public interface ListTeachersUseCase {
    Page<User> handle(ListTeacherQuery query);
}
