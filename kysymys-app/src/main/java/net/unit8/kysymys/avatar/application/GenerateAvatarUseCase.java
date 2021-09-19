package net.unit8.kysymys.avatar.application;

public interface GenerateAvatarUseCase {
    AvatarGeneratedEvent handle(GenerateAvatarCommand command);
}
