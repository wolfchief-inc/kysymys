package net.unit8.kysymys.user.application;

import lombok.Value;
import net.unit8.kysymys.user.domain.UserProfileByOther;

public interface ShowUserProfileUseCase {
    UserProfileByOther handle(ShowUserProfileQuery query);

    @Value
    class ShowUserProfileQuery {
        String targetUserId;
        String viewerUserId;
    }
}
