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
        if (userRepository.findByEmail("user1@example.com").isPresent())
            return;

        UserJpaEntity user1 = new UserJpaEntity();
        user1.setId(new UserId().getValue());
        user1.setName("user1");
        user1.setEmail("user1@example.com");
        user1.setPassword(passwordEncoder.encode("password"));
        user1.setRoles(Set.of("STUDENT", "TEACHER"));
        tx.execute(status -> {
            userRepository.save(user1);
            return null;
        });
    }
}
