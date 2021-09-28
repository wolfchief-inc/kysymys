package net.unit8.kysymys.user.adapter.persistence;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import net.unit8.kysymys.user.domain.Roles;
import net.unit8.kysymys.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
@DataJpaTest
@Import({UserPersistenceAdapter.class, UserMapper.class})
class UserPersistenceAdapterTest {
    @Autowired
    private UserPersistenceAdapter sut;

    @Autowired
    private UserRepository userRepository;

    private static final List<UserJpaEntity> USER_LIST_1 = List.of(
            user("ABC", "abc@example.com", Set.of("TEACHER")),
            user("DEF", "def@example.com", Set.of("STUDENT")),
            user("GHI", "ghi@example.com", Set.of("TEACHER", "STUDENT")),
            user("JKL", "jkl@example.com", Set.of())
    );
    @Test
    void withoutQuery() {
        userRepository.saveAllAndFlush(USER_LIST_1);

        Page<User> users = sut.list(null, 0);
        assertThat(users.getContent().size()).isEqualTo(USER_LIST_1.size());
    }

    @Test
    void withQuery() {
        userRepository.saveAllAndFlush(USER_LIST_1);

        Page<User> users = sut.list("h", 0);
        assertThat(users.getContent().size()).isEqualTo(1);
        assertThat(users.getContent().get(0)).hasFieldOrPropertyWithValue("name", "GHI");
    }

    @Test
    void withRole() {
        userRepository.saveAllAndFlush(USER_LIST_1);
        Page<User> users = sut.list("", Roles.of(Set.of("TEACHER")), 0);
        assertThat(users.getContent().size()).isEqualTo(2);

        Page<User> users2 = sut.list("", Roles.of(Set.of("TEACHER", "STUDENT")), 0);
        assertThat(users2.getContent().size()).isEqualTo(3);
    }

    private static UserJpaEntity user(String name, String email, Set<String> roles) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(NanoIdUtils.randomNanoId());
        entity.setName(name);
        entity.setEmail(email);
        entity.setRoles(roles);
        return entity;
    }
}