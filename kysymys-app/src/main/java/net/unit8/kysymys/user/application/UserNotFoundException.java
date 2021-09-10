package net.unit8.kysymys.user.application;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String userId) {
        super(userId);
    }
}
