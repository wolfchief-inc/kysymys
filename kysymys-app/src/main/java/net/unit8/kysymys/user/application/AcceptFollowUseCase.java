package net.unit8.kysymys.user.application;

import net.unit8.kysymys.user.domain.AcceptedFollowEvent;

public interface AcceptFollowUseCase {
    AcceptedFollowEvent handle(AcceptFollowCommand command);
}
