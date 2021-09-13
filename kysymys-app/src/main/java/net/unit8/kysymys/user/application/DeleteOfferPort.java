package net.unit8.kysymys.user.application;

import net.unit8.kysymys.user.domain.Offer;

public interface DeleteOfferPort {
    void delete(Offer offer);
}
