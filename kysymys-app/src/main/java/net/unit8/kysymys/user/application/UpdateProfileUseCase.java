package net.unit8.kysymys.user.application;

import lombok.Value;

import java.time.LocalDateTime;

public interface UpdateProfileUseCase {
    ProfileUpdatedEvent handle(UpdateProfileCommand command) throws PasswordMismatchException;

    @Value
    class UpdateProfileCommand {
        String userId;
        String name;
        String oldPassword;
        String newPassword;
    }

    @Value
    class ProfileUpdatedEvent {
        String userId;
        LocalDateTime occurredAt;
    }
}
