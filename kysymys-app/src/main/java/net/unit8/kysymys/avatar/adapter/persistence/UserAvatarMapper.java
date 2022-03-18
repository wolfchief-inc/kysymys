package net.unit8.kysymys.avatar.adapter.persistence;

import net.unit8.kysymys.avatar.domain.UserAvatar;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.stereotype.Component;

@Component
class UserAvatarMapper {
    UserAvatarJpaEntity domainToEntity(UserAvatar userAvatar) {
        UserAvatarJpaEntity entity = new UserAvatarJpaEntity();
        entity.setUserId(userAvatar.getUserId().asString());
        entity.setImageContent(userAvatar.getImage());
        return entity;
    }

    UserAvatar entityToDomain(UserAvatarJpaEntity entity) {
        return UserAvatar.of(UserId.of(entity.getUserId()), entity.getImageContent());
    }
}
