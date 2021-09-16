package net.unit8.kysymys.notification.adapter.persistence;

import net.unit8.kysymys.notification.application.ListWhatsNewPort;
import net.unit8.kysymys.notification.application.SaveWhatsNewPort;
import net.unit8.kysymys.notification.domain.WhatsNew;
import net.unit8.kysymys.stereotype.PersistenceAdapter;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.data.domain.*;

@PersistenceAdapter
class WhatsNewPersistenceAdapter implements SaveWhatsNewPort, ListWhatsNewPort {
    private final WhatsNewRepository whatsNewRepository;
    private final WhatsNewMapper whatsNewMapper;

    public WhatsNewPersistenceAdapter(WhatsNewRepository whatsNewRepository, WhatsNewMapper whatsNewMapper) {
        this.whatsNewRepository = whatsNewRepository;
        this.whatsNewMapper = whatsNewMapper;
    }

    @Override
    public Page<WhatsNew> findLatest(UserId userId, int size) {
        Pageable pageable = PageRequest.of(0, size, Sort.by("postedAt").descending());
        WhatsNewJpaEntity example = new WhatsNewJpaEntity();
        example.setUserId(userId.getValue());
        return whatsNewRepository.findAll(Example.of(example), pageable)
                .map(whatsNewMapper::entityToDomain);
    }

    @Override
    public void save(WhatsNew whatsNew) {
        whatsNewRepository.save(whatsNewMapper.domainToEntity(whatsNew));
    }
}
