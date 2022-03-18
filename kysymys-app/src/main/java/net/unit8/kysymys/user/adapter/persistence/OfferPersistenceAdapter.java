package net.unit8.kysymys.user.adapter.persistence;

import net.unit8.kysymys.stereotype.PersistenceAdapter;
import net.unit8.kysymys.user.application.*;
import net.unit8.kysymys.user.domain.Offer;
import net.unit8.kysymys.user.domain.OfferId;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

@PersistenceAdapter
class OfferPersistenceAdapter implements LoadOfferPort, SaveOfferPort, DeleteOfferPort, HasAlreadyOfferedPort, ListOffersPort {
    private final OfferRepository offerRepository;
    private final OfferMapper offerMapper;

    OfferPersistenceAdapter(OfferRepository offerRepository, OfferMapper offerMapper) {
        this.offerRepository = offerRepository;
        this.offerMapper = offerMapper;
    }

    @Override
    public Optional<Offer> load(OfferId offerId) {
        return offerRepository.findById(offerId.asString())
                .map(offerMapper::entityToDomain);
    }

    @Override
    public Page<Offer> listByTargetUser(UserId targetUserId, int page, int size) {
        if (page > 0) page = page - 1;
        return offerRepository.findAllByTargetUser(targetUserId.asString(), PageRequest.of(page, size))
                .map(offerMapper::entityToDomain);
    }

    @Override
    public void save(Offer offer) {
        offerRepository.save(offerMapper.domainToEntity(offer));
    }

    @Override
    public void delete(Offer offer) {
        offerRepository.delete(offerMapper.domainToEntity(offer));
    }

    @Override
    public boolean hasAlreadyOffered(UserId offeringUserId, UserId targetUserId) {
        return offerRepository.findByOfferingUserAndTargetUser(offeringUserId.asString(), targetUserId.asString())
                .isPresent();

    }
}
