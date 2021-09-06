package net.unit8.kysymys.user.adapter.persistence;

import net.unit8.kysymys.user.domain.*;
import org.springframework.stereotype.Component;

@Component
class UserMapper {
    public UserJpaEntity domainToEntity(User member) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(member.getId().getValue());
        entity.setName(member.getName().getValue());
        entity.setEmail(member.getEmail().toString());
        entity.setPassword(member.getPassword());
        return entity;
    }

    public User entityToDomain(UserJpaEntity entity) {
        return User.of(
                UserId.of(entity.getId()),
                EmailAddress.of(entity.getEmail()),
                UserName.of(entity.getName()),
                Password.ofEncoded(entity.getPassword())
        );
    }
}
