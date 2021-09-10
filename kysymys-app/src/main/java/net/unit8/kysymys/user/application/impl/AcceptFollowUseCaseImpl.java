package net.unit8.kysymys.user.application.impl;

import net.unit8.kysymys.user.application.AcceptFollowCommand;
import net.unit8.kysymys.user.application.AcceptFollowUseCase;
import net.unit8.kysymys.user.domain.AcceptedFollowEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
class AcceptFollowUseCaseImpl implements AcceptFollowUseCase {

    private final TransactionTemplate tx;
    AcceptFollowUseCaseImpl(TransactionTemplate tx) {
        this.tx = tx;
    }

    @Override
    public AcceptedFollowEvent handle(AcceptFollowCommand command) {

        return new AcceptedFollowEvent();
    }
}
