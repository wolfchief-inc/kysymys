package net.unit8.kysymys.avatar.adapter.persistence;

import net.unit8.kysymys.avatar.application.LoadAvatarPort;
import net.unit8.kysymys.avatar.application.SaveAvatarPort;
import net.unit8.kysymys.avatar.domain.UserAvatar;
import net.unit8.kysymys.stereotype.PersistenceAdapter;
import net.unit8.kysymys.user.domain.UserId;

import java.util.Optional;

@PersistenceAdapter
class UserAvatarPersistenceAdapter implements LoadAvatarPort, SaveAvatarPort {
    private final UserAvatarRepository userAvatarRepository;
    private final UserAvatarMapper userAvatarMapper;

    UserAvatarPersistenceAdapter(UserAvatarRepository userAvatarRepository, UserAvatarMapper userAvatarMapper) {
        this.userAvatarRepository = userAvatarRepository;
        this.userAvatarMapper = userAvatarMapper;
    }

    @Override
    public void save(UserAvatar userAvatar) {
        userAvatarRepository.save(userAvatarMapper.domainToEntity(userAvatar));
    }

    @Override
    public Optional<UserAvatar> load(UserId userId) {
        return userAvatarRepository.findById(userId.asString())
                .map(userAvatarMapper::entityToDomain);
    }
}
