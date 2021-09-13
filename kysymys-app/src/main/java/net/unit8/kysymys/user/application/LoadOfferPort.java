package net.unit8.kysymys.user.application;

import net.unit8.kysymys.user.domain.Offer;
import net.unit8.kysymys.user.domain.OfferId;

import java.util.Optional;

public interface LoadOfferPort {
    Optional<Offer> load(OfferId offerId);
}
