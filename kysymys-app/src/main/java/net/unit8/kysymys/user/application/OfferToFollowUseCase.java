package net.unit8.kysymys.user.application;

import net.unit8.kysymys.user.domain.OfferedToFollowEvent;

public interface OfferToFollowUseCase {
    OfferedToFollowEvent handle(OfferToFollowCommand command);
}
