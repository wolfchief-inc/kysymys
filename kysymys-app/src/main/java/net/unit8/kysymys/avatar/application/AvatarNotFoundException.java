package net.unit8.kysymys.avatar.application;

public class AvatarNotFoundException extends RuntimeException {
    public AvatarNotFoundException(String userId) {
        super(userId);
    }
}
