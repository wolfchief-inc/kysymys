package net.unit8.kysymys.user.application;

import lombok.Value;
import net.unit8.kysymys.user.domain.OfferedToFollowEvent;

public interface OfferToFollowUseCase {
    OfferedToFollowEvent handle(OfferToFollowCommand command);

    @Value
    class OfferToFollowCommand {
        String offeringUserId;
        String targetUserId;
    }
}
