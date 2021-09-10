package net.unit8.kysymys.user.application;

import net.unit8.kysymys.user.domain.Offer;

public interface SaveOfferPort {
    void save(Offer offer);
}
