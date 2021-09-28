package net.unit8.kysymys.user.adapter.persistence;

import net.unit8.kysymys.user.application.*;
import net.unit8.kysymys.user.domain.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
    public Page<User> list(String query, Roles roles, int page) {
        if (page > 0) page = page - 1;
        Pageable pageable = PageRequest.of(page, 10);

        List<Specification<UserJpaEntity>> specifications = new ArrayList<>();

        if (query != null && !query.isBlank()) {
            specifications.add(UserSpecs.containingQuery(query));
        }
        if (!roles.isEmpty()) {
            specifications.add(UserSpecs.hasRole(roles.stream().map(Role::name).collect(Collectors.toSet())));
        }

        return specifications.stream().reduce(Specification::and)
                .map(specs -> userRepository.findAll(specs, pageable))
                .orElseGet(() -> userRepository.findAll(pageable))
                .map(userMapper::entityToDomain);
    }

    @Override
    public Page<User> list(String query, int page) {
        return list(query, Roles.of(Set.of()), page);
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
