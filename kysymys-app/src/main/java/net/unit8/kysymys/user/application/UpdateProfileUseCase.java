package net.unit8.kysymys.user.application;

import lombok.Value;
import net.unit8.kysymys.user.domain.ProfileUpdatedEvent;

public interface UpdateProfileUseCase {
    ProfileUpdatedEvent handle(UpdateProfileCommand command);

    @Value
    class UpdateProfileCommand {
        String userId;
        String name;
        String oldPassword;
        String newPassword;
    }
}
