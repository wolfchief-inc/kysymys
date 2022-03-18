package net.unit8.kysymys.notification.adapter.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.unit8.kysymys.notification.domain.TemplatePath;
import net.unit8.kysymys.notification.domain.WhatsNew;
import net.unit8.kysymys.notification.domain.WhatsNewId;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UncheckedIOException;
import java.util.Map;

@Component
class WhatsNewMapper {
    private final TemplateEngine templateEngine;
    private final ObjectMapper objectMapper;

    WhatsNewMapper(@Qualifier("notificationTemplateEngine") TemplateEngine templateEngine, ObjectMapper objectMapper) {
        this.templateEngine = templateEngine;
        this.objectMapper = objectMapper;
    }

    public WhatsNew entityToDomain(WhatsNewJpaEntity entity) {
        Map<String, Object> params = deserialize(entity.getParams());
        Context context = new Context();
        params.forEach(context::setVariable);
        String messageBody = templateEngine.process(entity.getTemplatePath(), context);
        return WhatsNew.of(WhatsNewId.of(entity.getId()),
                UserId.of(entity.getUserId()),
                TemplatePath.of(entity.getTemplatePath()),
                params,
                entity.getPostedAt(),
                messageBody);
    }

    public WhatsNewJpaEntity domainToEntity(WhatsNew whatsNew) {
        WhatsNewJpaEntity entity = new WhatsNewJpaEntity();
        entity.setId(whatsNew.getId().asString());
        entity.setUserId(whatsNew.getUserId().asString());
        entity.setTemplatePath(whatsNew.getTemplatePath().asString());
        try {
            entity.setParams(objectMapper.writeValueAsString(whatsNew.getParams()));
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
        entity.setPostedAt(whatsNew.getPostedAt());
        return entity;
    }

    private Map<String, Object> deserialize(String body) {
        try {
            return objectMapper.readValue(body, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }
}
