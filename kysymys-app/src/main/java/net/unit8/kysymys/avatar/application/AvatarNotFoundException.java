package net.unit8.kysymys.avatar.application;

import net.unit8.kysymys.user.domain.UserId;

public class AvatarNotFoundException extends RuntimeException {
    public AvatarNotFoundException(String userId) {
        super(userId);
    }
}
