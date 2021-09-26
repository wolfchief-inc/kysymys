package net.unit8.kysymys.user.application;

import net.unit8.kysymys.user.domain.UserCreatedEvent;

public interface SignupUseCase {
    UserCreatedEvent handle(SignupCommand command);
}
