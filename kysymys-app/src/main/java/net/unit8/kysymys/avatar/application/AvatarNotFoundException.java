package net.unit8.kysymys.avatar.application;

public class AvatarNotFoundException extends Exception {
    public AvatarNotFoundException(String userId) {
        super(userId);
    }
}
