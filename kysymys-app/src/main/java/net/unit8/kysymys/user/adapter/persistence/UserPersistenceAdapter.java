package net.unit8.kysymys.user.adapter.persistence;

import net.unit8.kysymys.user.domain.EmailAddress;
import net.unit8.kysymys.user.domain.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
class UserPersistenceAdapter implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    UserPersistenceAdapter(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(userMapper::entityToDomain)
                .orElseThrow(() -> new UsernameNotFoundException(email));
    }

    @Override
    public void save(User user) {
        userRepository.save(userMapper.domainToEntity(user));
    }

    @Override
    public boolean exists(EmailAddress emailAddress) {
        return userRepository.findByEmail(emailAddress.toString()).isPresent();
    }

}
