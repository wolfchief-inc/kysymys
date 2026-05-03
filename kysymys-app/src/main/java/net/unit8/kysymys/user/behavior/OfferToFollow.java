package net.unit8.kysymys.user.behavior;

import net.unit8.kysymys.events.OfferedToFollowEvent;
import net.unit8.kysymys.system.KysymysEventBus;
import net.unit8.kysymys.user.dao.OfferDao;
import net.unit8.kysymys.user.dao.UserDao;
import net.unit8.kysymys.user.data.Offer;
import net.unit8.kysymys.user.data.OfferId;
import net.unit8.kysymys.user.data.User;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;

import java.time.LocalDateTime;
import java.util.Optional;

public class OfferToFollow {
    private final DSLContext dsl;
    private final KysymysEventBus eventBus;

    public OfferToFollow(DSLContext dsl, KysymysEventBus eventBus) {
        this.dsl = dsl;
        this.eventBus = eventBus;
    }

    public Optional<Offer> apply(Input in) {
        UserDao users = new UserDao(dsl);
        Optional<User> offering = users.findById(in.offeringUserId());
        Optional<User> target = users.findById(in.targetUserId());
        if (offering.isEmpty() || target.isEmpty()) return Optional.empty();
        if (in.offeringUserId().equals(in.targetUserId())) return Optional.empty();

        OfferDao offerDao = new OfferDao(dsl);
        if (offerDao.alreadyExists(in.offeringUserId(), in.targetUserId())) {
            return Optional.empty();
        }

        Offer offer = new Offer(OfferId.newId(),
                in.offeringUserId(), in.targetUserId(), in.now());
        dsl.transaction(cfg -> new OfferDao(cfg.dsl()).insert(offer));

        if (eventBus != null) {
            eventBus.publish(new OfferedToFollowEvent(
                    offer.id(),
                    offering.get().id(), offering.get().name(),
                    target.get().id(), target.get().name(),
                    target.get().email(),
                    in.now()));
        }
        return Optional.of(offer);
    }

    public record Input(UserId offeringUserId, UserId targetUserId, LocalDateTime now) {}
}
