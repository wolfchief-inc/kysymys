package net.unit8.kysymys.notification.adapter.persistence;

import net.unit8.kysymys.notification.domain.WhatsNew;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class WhatsNewPersistenceAdapter {
    private final WhatsNewRepository whatsNewRepository;
    private final WhatsNewMapper whatsNewMapper;

    public WhatsNewPersistenceAdapter(WhatsNewRepository whatsNewRepository, WhatsNewMapper whatsNewMapper) {
        this.whatsNewRepository = whatsNewRepository;
        this.whatsNewMapper = whatsNewMapper;
    }

    public Page<WhatsNew> findLatest(UserId userId, int size) {
        Pageable pageable = PageRequest.of(0, size);
        return whatsNewRepository.findAll(pageable)
                .map(whatsNewMapper::entityToDomain);
    }
}
