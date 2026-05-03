package net.unit8.kysymys.notification.data;

import net.unit8.kysymys.user.data.UserId;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

public record WhatsNew(
        WhatsNewId id,
        UserId userId,
        TemplatePath templatePath,
        Map<String, Object> params,
        LocalDateTime postedAt
) {
    public WhatsNew {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(templatePath, "templatePath");
        params = params == null ? Map.of() : Map.copyOf(params);
        Objects.requireNonNull(postedAt, "postedAt");
    }
}
