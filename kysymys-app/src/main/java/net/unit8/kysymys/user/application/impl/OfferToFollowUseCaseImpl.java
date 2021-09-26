package net.unit8.kysymys.user.application.impl;

import am.ik.yavi.core.ConstraintViolationsException;
import net.unit8.kysymys.share.application.CurrentDateTimePort;
import net.unit8.kysymys.stereotype.UseCase;
import net.unit8.kysymys.user.application.*;
import net.unit8.kysymys.user.domain.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

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

        LocalDateTime now = currentDateTimePort.now();
        Offer offer = Offer.of(new OfferId(), offeringUser, targetUser, now);
        return tx.execute(status -> {
            saveOfferPort.save(offer);
            OfferedToFollowEvent event = new OfferedToFollowEvent(
                    targetUser.getId().getValue(), targetUser.getName(),
                    targetUser.getEmail().getValue(),
                    offeringUser.getId().getValue(), offeringUser.getName(),
                    now);
            applicationEventPublisher.publishEvent(event);
            return event;
        });

    }
}
