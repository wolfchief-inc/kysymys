package net.unit8.kysymys.user.application;

public interface UpdateProfileUseCase {
    ProfileUpdatedEvent handle(UpdateProfileCommand command);
}
