package net.unit8.kysymys.avatar.application;

import net.unit8.kysymys.avatar.domain.UserAvatar;

public interface SaveAvatarPort {
    void save(UserAvatar userAvatar);
}
