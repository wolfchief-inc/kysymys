package net.unit8.kysymys.user.application;

import net.unit8.kysymys.user.domain.UserProfileByOther;

public interface ShowUserProfileUseCase {
    UserProfileByOther handle(ShowUserProfileQuery query);
}
