package net.unit8.kysymys.user.application.impl;

import am.ik.yavi.core.ConstraintViolationsException;
import net.unit8.kysymys.share.application.CurrentDateTimePort;
import net.unit8.kysymys.steleotype.UseCase;
import net.unit8.kysymys.user.application.*;
import net.unit8.kysymys.user.domain.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@UseCase
class OfferToFollowUseCaseImpl implements OfferToFollowUseCase {
    private final LoadUserPort loadUserPort;
    private final SaveOfferPort saveOfferPort;
    private final CurrentDateTimePort currentDateTimePort;
    private final TransactionTemplate tx;
    private final ApplicationEventPublisher applicationEventPublisher;

    OfferToFollowUseCaseImpl(LoadUserPort loadUserPort, SaveOfferPort saveOfferPort, CurrentDateTimePort currentDateTimePort, TransactionTemplate tx, ApplicationEventPublisher applicationEventPublisher) {
        this.loadUserPort = loadUserPort;
        this.saveOfferPort = saveOfferPort;
        this.currentDateTimePort = currentDateTimePort;
        this.tx = tx;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public OfferedToFollowEvent handle(OfferToFollowCommand command) {
        User offeringUser = UserId.validator.validate(command.getOfferingUserId())
                .map(loadUserPort::load)
                .map(user -> user.orElseThrow(() -> new UserNotFoundException(command.getOfferingUserId())))
                .orElseThrow(ConstraintViolationsException::new);
        User targetUser = UserId.validator.validate(command.getTargetUserId())
                .map(loadUserPort::load)
                .map(user -> user.orElseThrow(() -> new UserNotFoundException(command.getTargetUserId())))
                .orElseThrow(ConstraintViolationsException::new);

        Offer offer = Offer.of(new OfferId(), offeringUser, targetUser, currentDateTimePort.now());
        return tx.execute(status -> {
            saveOfferPort.save(offer);
            OfferedToFollowEvent event = new OfferedToFollowEvent(targetUser);
            applicationEventPublisher.publishEvent(event);
            return event;
        });

    }
}