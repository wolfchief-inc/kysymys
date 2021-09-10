package net.unit8.kysymys.user.application.impl;

import am.ik.yavi.core.ConstraintViolationsException;
import am.ik.yavi.core.Validated;
import net.unit8.kysymys.user.application.LoadUserPort;
import net.unit8.kysymys.user.application.OfferToFollowCommand;
import net.unit8.kysymys.user.application.OfferToFollowUseCase;
import net.unit8.kysymys.user.application.SaveOfferPort;
import net.unit8.kysymys.user.domain.Offer;
import net.unit8.kysymys.user.domain.OfferedToFollowEvent;
import net.unit8.kysymys.user.domain.User;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
class OfferToFollowUseCaseImpl implements OfferToFollowUseCase {
    private final LoadUserPort loadUserPort;
    private final SaveOfferPort saveOfferPort;
    private final TransactionTemplate tx;

    OfferToFollowUseCaseImpl(LoadUserPort loadUserPort, SaveOfferPort saveOfferPort, TransactionTemplate tx) {
        this.loadUserPort = loadUserPort;
        this.saveOfferPort = saveOfferPort;
        this.tx = tx;
    }

    @Override
    public OfferedToFollowEvent handle(OfferToFollowCommand command) {
        User offeringUser = UserId.validator.validate(command.getOfferingUserId())
                .map(loadUserPort::load)
                .map(user -> user.orElseThrow())
                .orElseThrow(ConstraintViolationsException::new);
        User targetUser = UserId.validator.validate(command.getTargetUserId())
                .map(loadUserPort::load)
                .map(user -> user.orElseThrow())
                .orElseThrow(ConstraintViolationsException::new);

        Offer offer = Offer.of(offeringUser, targetUser);
        return tx.execute(status -> {
            saveOfferPort.save(offer);
            return new OfferedToFollowEvent(targetUser);
        });

    }
}
