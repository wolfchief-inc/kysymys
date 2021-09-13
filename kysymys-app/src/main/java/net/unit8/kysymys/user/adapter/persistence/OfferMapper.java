package net.unit8.kysymys.user.adapter.persistence;

import net.unit8.kysymys.user.domain.Offer;
import net.unit8.kysymys.user.domain.OfferId;
import org.springframework.stereotype.Component;

@Component
class OfferMapper {
    private final UserMapper userMapper;

    OfferMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    OfferJpaEntity domainToEntity(Offer offer) {
        OfferJpaEntity entity = new OfferJpaEntity();
        entity.setId(offer.getId().getValue());
        entity.setOfferingUser(userMapper.domainToEntity(offer.getOfferingUser()));
        entity.setTargetUser(userMapper.domainToEntity(offer.getTargetUser()));
        entity.setOfferedAt(offer.getOfferedAt());
        return entity;
    }

    Offer entityToDomain(OfferJpaEntity entity) {
        return new Offer(
                OfferId.of(entity.getId()),
                userMapper.entityToDomain(entity.getOfferingUser()),
                userMapper.entityToDomain(entity.getTargetUser()),
                entity.getOfferedAt()
        );
    }
}
