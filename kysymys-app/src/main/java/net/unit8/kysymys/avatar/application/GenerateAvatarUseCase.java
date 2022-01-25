package net.unit8.kysymys.avatar.application;

import lombok.Value;

import java.io.InputStream;

public interface GenerateAvatarUseCase {
    AvatarGeneratedEvent handle(GenerateAvatarCommand command);

    @Value
    class AvatarGeneratedEvent {
        InputStream image;
    }

    @Value
    class GenerateAvatarCommand {
        String userId;
    }
}
