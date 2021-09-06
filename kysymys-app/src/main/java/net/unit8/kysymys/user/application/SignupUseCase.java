package net.unit8.kysymys.user.application;

import net.unit8.kysymys.user.domain.CreatedUserEvent;

public interface SignupUseCase {
    CreatedUserEvent handle(SignupCommand command);
}
