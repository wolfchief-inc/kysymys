package net.unit8.kysymys.user.application;

import lombok.Value;
import net.unit8.kysymys.user.domain.UserCreatedEvent;

public interface SignupUseCase {
    UserCreatedEvent handle(SignupCommand command) throws EmailAlreadyTakenException;

    @Value
    class SignupCommand {
        String email;
        String name;
        String password;
    }
}
