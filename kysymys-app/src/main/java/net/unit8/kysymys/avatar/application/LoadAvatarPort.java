package net.unit8.kysymys.avatar.application;

import net.unit8.kysymys.avatar.domain.UserAvatar;
import net.unit8.kysymys.user.domain.UserId;

import java.util.Optional;

public interface LoadAvatarPort {
    Optional<UserAvatar> load(UserId userId);
}
