package net.unit8.kysymys.user.application;

import lombok.Value;

import java.time.LocalDateTime;

public interface AcceptFollowUseCase {
    AcceptedFollowEvent handle(AcceptFollowCommand command) throws OfferNotFound;

    @Value
    class AcceptFollowCommand {
        String offerId;
        String acceptorId;
    }

    @Value
    class AcceptedFollowEvent {
        LocalDateTime occurredAt;
    }
}
