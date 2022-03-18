package net.unit8.kysymys.user.application;

import lombok.Value;

import java.time.LocalDateTime;

public interface OfferToFollowUseCase {
    OfferedToFollowEvent handle(OfferToFollowCommand command);

    @Value
    class OfferToFollowCommand {
        String offeringUserId;
        String targetUserId;
    }

    @Value
    class OfferedToFollowEvent {
        String targetUserId;
        String targetUserName;
        String targetUserEmail;
        String offeringUserId;
        String offeringUserName;
        LocalDateTime occurredAt;
    }
}
