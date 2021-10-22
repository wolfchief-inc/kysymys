package net.unit8.kysymys.notification.adapter.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.stream.Stream;

public interface UnreadWhatsNewRepository extends JpaRepository<UnreadWhatsNewJpaEntity, WhatsNewJpaEntity> {
    @Query("SELECT uwn FROM unreadWhatsNew uwn JOIN uwn.whatsNew wn WHERE wn.userId = :userId ORDER BY wn.postedAt DESC")
    Page<UnreadWhatsNewJpaEntity> findByUserId(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT uwn FROM unreadWhatsNew uwn JOIN uwn.whatsNew wn WHERE wn.userId=:userId "
            + "AND wn.id IN :whatsNewIds")
    Stream<UnreadWhatsNewJpaEntity> findAllByWhatsNewIds(@Param("userId") String userId, @Param("whatsNewIds") List<String> whatsNewIds);
}
