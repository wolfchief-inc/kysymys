package net.unit8.kysymys.user.adapter.persistence;

import net.unit8.kysymys.user.application.*;
import net.unit8.kysymys.user.domain.EmailAddress;
import net.unit8.kysymys.user.domain.User;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
class UserPersistenceAdapter implements UserDetailsService, LoadUserPort, SaveUserPort, ExistsEmailAddressPort, GetUsersPort {
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

    @Override
    public Page<User> list(String query, int page) {
        if (page > 0) page = page - 1;
        Pageable pageable = PageRequest.of(page, 10);

        if (query == null || query.isEmpty()) {
            return userRepository.findAll(pageable)
                    .map(userMapper::entityToDomain);
        } else {
            return userRepository.findByQuery(query, pageable)
                    .map(userMapper::entityToDomain);
        }
    }

    @Override
    public Set<User> listByUserIds(Set<UserId> userIds) {
        return userRepository.findAllByUserIds(userIds.stream().map(UserId::getValue).collect(Collectors.toSet()))
                .stream()
                .map(userMapper::entityToDomain)
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<User> load(UserId userId) {
        return userRepository.findById(userId.getValue())
                .map(userMapper::entityToDomain);
    }
}
