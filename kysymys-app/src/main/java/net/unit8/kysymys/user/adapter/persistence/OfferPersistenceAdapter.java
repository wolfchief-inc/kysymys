package net.unit8.kysymys.user.adapter.persistence;

import net.unit8.kysymys.user.application.SaveOfferPort;
import net.unit8.kysymys.user.domain.Offer;
import org.springframework.stereotype.Component;

@Component
class OfferPersistenceAdapter implements SaveOfferPort {
    @Override
    public void save(Offer offer) {

    }
}
