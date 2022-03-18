package net.unit8.kysymys.notification.adapter.persistence;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import net.unit8.kysymys.notification.application.DeleteAllWhatsNewsPort;
import net.unit8.kysymys.notification.application.ListWhatsNewPort;
import net.unit8.kysymys.notification.application.SaveWhatsNewPort;
import net.unit8.kysymys.notification.domain.WhatsNew;
import net.unit8.kysymys.notification.domain.WhatsNewId;
import net.unit8.kysymys.stereotype.PersistenceAdapter;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

@PersistenceAdapter
class WhatsNewPersistenceAdapter implements SaveWhatsNewPort, ListWhatsNewPort, DeleteAllWhatsNewsPort {
    private final WhatsNewRepository whatsNewRepository;
    private final UnreadWhatsNewRepository unreadWhatsNewRepository;
    private final WhatsNewMapper whatsNewMapper;

    public WhatsNewPersistenceAdapter(WhatsNewRepository whatsNewRepository, UnreadWhatsNewRepository unreadWhatsNewRepository, WhatsNewMapper whatsNewMapper) {
        this.whatsNewRepository = whatsNewRepository;
        this.unreadWhatsNewRepository = unreadWhatsNewRepository;
        this.whatsNewMapper = whatsNewMapper;
    }

    @Override
    public Page<WhatsNew> findLatestUnread(UserId userId, int size) {
        Pageable pageable = PageRequest.of(0, size);
        return unreadWhatsNewRepository.findByUserId(userId.asString(), pageable)
                .map(UnreadWhatsNewJpaEntity::getWhatsNew)
                .map(whatsNewMapper::entityToDomain);
    }

    @Override
    public void save(WhatsNew whatsNew) {
        WhatsNewJpaEntity entity = whatsNewMapper.domainToEntity(whatsNew);
        whatsNewRepository.save(entity);
        UnreadWhatsNewJpaEntity unreadEntity = new UnreadWhatsNewJpaEntity();
        unreadEntity.setId(NanoIdUtils.randomNanoId());
        unreadEntity.setWhatsNew(entity);
        unreadWhatsNewRepository.save(unreadEntity);
    }

    @Override
    public void deleteAll(UserId userId, List<WhatsNewId> whatsNewIds) {
        unreadWhatsNewRepository.findAllByWhatsNewIds(userId.asString(), whatsNewIds.stream().map(WhatsNewId::asString).collect(Collectors.toList()))
                .forEach(unreadWhatsNewRepository::delete);
    }
}
