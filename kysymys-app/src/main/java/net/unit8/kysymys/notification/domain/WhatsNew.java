package net.unit8.kysymys.notification.domain;

import am.ik.yavi.arguments.*;
import am.ik.yavi.builder.ArgumentsValidatorBuilder;
import lombok.Value;
import lombok.experimental.NonFinal;
import net.unit8.kysymys.user.domain.UserId;

import java.time.LocalDateTime;
import java.util.Map;

@Value
public class WhatsNew {
    private static final Arguments5Validator<WhatsNewId, UserId, TemplatePath, Map<String,Object>, LocalDateTime, WhatsNew> validator = ArgumentsValidatorBuilder.of(WhatsNew::new)
            .builder(b -> b._object(Arguments1::arg1, "whatsNewId", c -> c.notNull()))
            .builder(b -> b._object(Arguments2::arg2, "userId", c -> c.notNull()))
            .builder(b -> b._object(Arguments3::arg3, "templatePath", c -> c.notNull()))
            .builder(b -> b._object(Arguments5::arg5, "postedAt", c -> c.notNull()))
            .build();

    WhatsNewId id;
    UserId userId;
    TemplatePath templatePath;
    Map<String, Object> params;
    LocalDateTime postedAt;
    @NonFinal String message;

    private WhatsNew(WhatsNewId id, UserId userId, TemplatePath templatePath, Map<String, Object> params, LocalDateTime postedAt) {
        this.id = id;
        this.userId = userId;
        this.templatePath = templatePath;
        this.params = params;
        this.postedAt = postedAt;
        message = null;
    }

    public static WhatsNew of(WhatsNewId id, UserId userId, TemplatePath templatePath, Map<String, Object> params, LocalDateTime postedAt) {
        return validator.validated(id, userId, templatePath, params, postedAt);
    }

    public static WhatsNew of(WhatsNewId id, UserId userId, TemplatePath templatePath, Map<String, Object> params, LocalDateTime postedAt, String message) {
        WhatsNew whatsNew = validator.validated(id, userId, templatePath, params, postedAt);
        whatsNew.message = message;
        return whatsNew;
    }

}
