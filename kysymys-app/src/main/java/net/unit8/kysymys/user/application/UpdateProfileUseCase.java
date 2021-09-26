package net.unit8.kysymys.user.application;

import net.unit8.kysymys.user.domain.ProfileUpdatedEvent;

public interface UpdateProfileUseCase {
    ProfileUpdatedEvent handle(UpdateProfileCommand command);
}
