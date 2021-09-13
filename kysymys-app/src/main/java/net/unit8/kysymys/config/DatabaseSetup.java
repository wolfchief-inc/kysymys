package net.unit8.kysymys.config;

import net.unit8.kysymys.user.adapter.persistence.UserJpaEntity;
import net.unit8.kysymys.user.adapter.persistence.UserRepository;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Set;

@Component
public class DatabaseSetup implements InitializingBean {
    private final UserRepository userRepository;
    private final TransactionTemplate tx;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSetup(UserRepository userRepository, TransactionTemplate tx, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tx = tx;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (userRepository.findByEmail("teacher1@example.com").isPresent())
            return;

        UserJpaEntity teacher1 = new UserJpaEntity();
        teacher1.setId(new UserId().getValue());
        teacher1.setName("teacher1");
        teacher1.setEmail("teacher1@example.com");
        teacher1.setPassword(passwordEncoder.encode("password"));
        teacher1.setRoles(Set.of("STUDENT", "TEACHER"));

        UserJpaEntity student1 = new UserJpaEntity();
        student1.setId(new UserId().getValue());
        student1.setName("student1");
        student1.setEmail("student1@example.com");
        student1.setPassword(passwordEncoder.encode("password"));
        student1.setRoles(Set.of("STUDENT"));

        tx.execute(status -> {
            userRepository.save(teacher1);
            userRepository.save(student1);
            return null;
        });
    }
}
