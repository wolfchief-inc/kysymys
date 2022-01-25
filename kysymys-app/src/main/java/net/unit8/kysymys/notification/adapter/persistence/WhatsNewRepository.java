package net.unit8.kysymys.notification.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WhatsNewRepository extends JpaRepository<WhatsNewJpaEntity, String> {
}
