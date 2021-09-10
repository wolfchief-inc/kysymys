package net.unit8.kysymys.notification.adapter.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.unit8.kysymys.notification.domain.WhatsNew;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UncheckedIOException;
import java.util.Map;

@Component
class WhatsNewMapper {
    private final TemplateEngine templateEngine;
    private final ObjectMapper objectMapper;

    WhatsNewMapper(TemplateEngine templateEngine, ObjectMapper objectMapper) {
        this.templateEngine = templateEngine;
        this.objectMapper = objectMapper;
    }

    public WhatsNew entityToDomain(WhatsNewJpaEntity entity) {
        Map<String, Object> params = deserialize(entity.getParams());
        Context context = new Context();
        String messageBody = templateEngine.process(entity.getTemplatePath(), context);
        return new WhatsNew(messageBody);
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
