package net.unit8.kysymys.user.application;

import lombok.Value;
import net.unit8.kysymys.user.domain.AcceptedFollowEvent;

public interface AcceptFollowUseCase {
    AcceptedFollowEvent handle(AcceptFollowCommand command);

    @Value
    class AcceptFollowCommand {
        String offerId;
        String acceptorId;
    }
}
