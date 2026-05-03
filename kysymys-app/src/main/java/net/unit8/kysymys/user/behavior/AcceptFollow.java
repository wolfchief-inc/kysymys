package net.unit8.kysymys.user.behavior;

import net.unit8.kysymys.user.dao.ConnectionDao;
import net.unit8.kysymys.user.dao.OfferDao;
import net.unit8.kysymys.user.data.Offer;
import net.unit8.kysymys.user.data.OfferId;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;

import java.util.Optional;

public class AcceptFollow {
    private final DSLContext dsl;

    public AcceptFollow(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * Accepts the offer addressed to {@code accepter}. The acceptance creates
     * a connection (offering user becomes a follower of the target user) and
     * removes the offer.
     *
     * @return {@code true} if accepted, {@code false} if the offer doesn't
     *         exist or the accepter isn't its target.
     */
    public boolean apply(OfferId offerId, UserId accepter) {
        OfferDao offerDao = new OfferDao(dsl);
        Optional<Offer> offer = offerDao.findById(offerId);
        if (offer.isEmpty()) return false;
        if (!offer.get().targetUserId().equals(accepter)) return false;

        dsl.transaction(cfg -> {
            new ConnectionDao(cfg.dsl()).add(
                    offer.get().targetUserId(),    // followee
                    offer.get().offeringUserId()); // follower
            new OfferDao(cfg.dsl()).delete(offerId);
        });
        return true;
    }
}
