package net.unit8.kysymys.avatar.application;

public interface GetAvatarImageUseCase {
    byte[] handle(String userId) throws AvatarNotFoundException;
}
