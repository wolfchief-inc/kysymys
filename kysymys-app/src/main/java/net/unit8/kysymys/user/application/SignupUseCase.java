package net.unit8.kysymys.user.application;

import lombok.Value;

import java.time.LocalDateTime;

public interface SignupUseCase {
    UserCreatedEvent handle(SignupCommand command) throws EmailAlreadyTakenException;

    @Value
    class SignupCommand {
        String email;
        String name;
        String password;
    }

    @Value
    class UserCreatedEvent {
        String userId;
        LocalDateTime occurredAt;
    }
}
