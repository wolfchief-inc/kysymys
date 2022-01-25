package net.unit8.kysymys.avatar.application.impl;

import net.unit8.kysymys.avatar.application.AvatarNotFoundException;
import net.unit8.kysymys.avatar.application.GetAvatarImageUseCase;
import net.unit8.kysymys.avatar.application.LoadAvatarPort;
import net.unit8.kysymys.avatar.domain.UserAvatar;
import net.unit8.kysymys.stereotype.UseCase;
import net.unit8.kysymys.user.domain.UserId;

import java.util.Optional;

@UseCase
class GetAvatarImageUseCaseImpl implements GetAvatarImageUseCase {
    private final LoadAvatarPort loadAvatarPort;

    public GetAvatarImageUseCaseImpl(LoadAvatarPort loadAvatarPort) {
        this.loadAvatarPort = loadAvatarPort;
    }

    @Override
    public byte[] handle(String userId) throws AvatarNotFoundException {
        Optional<UserAvatar> userAvatar = loadAvatarPort.load(UserId.of(userId));
        return userAvatar.map(UserAvatar::getImage).orElseThrow(() -> new AvatarNotFoundException(userId));
    }
}
