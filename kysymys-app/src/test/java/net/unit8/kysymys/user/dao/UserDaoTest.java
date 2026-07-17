package net.unit8.kysymys.user.dao;

import net.unit8.kysymys.lesson.dao.DaoTestSupport;
import net.unit8.kysymys.lesson.data.IdGenerator;
import net.unit8.kysymys.user.data.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserDaoTest {
    private static final DaoTestSupport support = new DaoTestSupport();
    private static final UserDao dao = new UserDao(support.dsl());

    @Test
    void upsertInsertsThenUpdates() {
        UserId id = UserId.of(IdGenerator.newId());
        User u1 = new User(id,
                EmailAddress.of("a@example.com"),
                UserName.of("Alice"),
                Roles.of(Set.of(Role.STUDENT)));
        support.dsl().transaction(cfg -> new UserDao(cfg.dsl()).upsert(u1));

        User u2 = new User(id,
                EmailAddress.of("a@example.com"),
                UserName.of("Alice Renamed"),
                Roles.of(Set.of(Role.STUDENT, Role.TEACHER)));
        support.dsl().transaction(cfg -> new UserDao(cfg.dsl()).upsert(u2));

        Optional<User> found = dao.findById(id);
        assertThat(found).hasValueSatisfying(u -> {
            assertThat(u.name().value()).isEqualTo("Alice Renamed");
            assertThat(u.roles().contains(Role.TEACHER)).isTrue();
        });
    }

    @Test
    void listSearchesByName() {
        support.dsl().transaction(cfg -> {
            UserDao d = new UserDao(cfg.dsl());
            d.upsert(new User(UserId.of(IdGenerator.newId()),
                    EmailAddress.of("ghi@example.com"),
                    UserName.of("Ghi"),
                    Roles.of(Set.of(Role.STUDENT))));
            d.upsert(new User(UserId.of(IdGenerator.newId()),
                    EmailAddress.of("xyz@example.com"),
                    UserName.of("Xyz"),
                    Roles.of(Set.of(Role.STUDENT))));
        });
        List<User> hits = dao.list("gh");
        assertThat(hits).extracting(u -> u.name().value()).contains("Ghi");
    }

    @Test
    void listByRole() {
        UserId teacherId = UserId.of(IdGenerator.newId());
        support.dsl().transaction(cfg -> {
            new UserDao(cfg.dsl()).upsert(new User(teacherId,
                    EmailAddress.of("t@example.com"),
                    UserName.of("Teacher"),
                    Roles.of(Set.of(Role.TEACHER))));
        });
        List<User> teachers = dao.listByRole(Role.TEACHER);
        assertThat(teachers).extracting(u -> u.id()).contains(teacherId);
    }
}
