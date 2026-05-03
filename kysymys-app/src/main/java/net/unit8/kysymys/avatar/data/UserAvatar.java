package net.unit8.kysymys.avatar.data;

import net.unit8.kysymys.user.data.UserId;

import java.util.Objects;

public record UserAvatar(UserId userId, byte[] image) {
    public UserAvatar {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(image, "image");
        if (image.length == 0) {
            throw new IllegalArgumentException("image must not be empty");
        }
    }
}
