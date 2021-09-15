package net.unit8.kysymys.user.application.impl;

import net.unit8.kysymys.stereotype.UseCase;
import net.unit8.kysymys.user.application.*;
import net.unit8.kysymys.user.domain.AcceptedFollowEvent;
import net.unit8.kysymys.user.domain.Offer;
import net.unit8.kysymys.user.domain.OfferId;
import org.springframework.transaction.support.TransactionTemplate;

@UseCase
class AcceptFollowUseCaseImpl implements AcceptFollowUseCase {
    private final LoadOfferPort loadOfferPort;
    private final DeleteOfferPort deleteOfferPort;
    private final AddFollowerPort addFollowerPort;
    private final TransactionTemplate tx;

    AcceptFollowUseCaseImpl(LoadOfferPort loadOfferPort, DeleteOfferPort deleteOfferPort, AddFollowerPort addFollowerPort, TransactionTemplate tx) {
        this.loadOfferPort = loadOfferPort;
        this.deleteOfferPort = deleteOfferPort;
        this.addFollowerPort = addFollowerPort;
        this.tx = tx;
    }

    @Override
    public AcceptedFollowEvent handle(AcceptFollowCommand command) {
        Offer offer = loadOfferPort.load(OfferId.of(command.getOfferId()))
                .orElseThrow(() -> new OfferNotFound(command.getOfferId()));
        if (!command.getAcceptorId().equals(offer.getTargetUser().getId().getValue())) {
            throw new OfferNotFound(command.getOfferId());
        }

        return tx.execute(status -> {
            deleteOfferPort.delete(offer);
            addFollowerPort.follow(offer.getOfferingUser().getId(),
                    offer.getTargetUser().getId());
            return new AcceptedFollowEvent();
        });
    }
}
