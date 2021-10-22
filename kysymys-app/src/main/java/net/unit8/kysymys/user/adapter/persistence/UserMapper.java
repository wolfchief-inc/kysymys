package net.unit8.kysymys.user.adapter.persistence;

import net.unit8.kysymys.user.domain.*;
import org.hibernate.collection.internal.PersistentSet;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Collectors;

@Component
class UserMapper {
    public UserJpaEntity domainToEntity(User member) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(member.getId().getValue());
        entity.setName(member.getName());
        entity.setEmail(member.getEmail().toString());
        entity.setPassword(member.getPassword());
        entity.setRoles(member.getRoles().stream().map(Enum::name).collect(Collectors.toSet()));
        entity.setFollowers(new PersistentSet());
        return entity;
    }

    public User entityToDomain(UserJpaEntity entity) {
        return User.of(
                UserId.of(entity.getId()),
                EmailAddress.of(entity.getEmail()),
                UserName.of(entity.getName()),
                Optional.ofNullable(entity.getPassword())
                        .map(Password::ofEncoded)
                        .orElse(Password.notSet()),
                Roles.of(entity.getRoles())
        );
    }
}
