package net.unit8.kysymys.user.adapter.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OfferRepository extends JpaRepository<OfferJpaEntity, String> {
    @Query("SELECT o FROM offer o WHERE o.offeringUser.id=?1 AND o.targetUser.id=?2")
    Optional<OfferJpaEntity> findByOfferingUserAndTargetUser(String offeringUserId, String targetUserId);

    @Query("SELECT o FROM offer o WHERE o.targetUser.id=?1 ORDER BY o.offeredAt DESC")
    Page<OfferJpaEntity> findAllByTargetUser(String targetUserId, Pageable pageable);
}
