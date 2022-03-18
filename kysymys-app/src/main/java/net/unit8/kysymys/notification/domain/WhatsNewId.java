package net.unit8.kysymys.notification.domain;

import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.builder.StringValidatorBuilder;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

@EqualsAndHashCode
public class WhatsNewId {
    public static final StringValidator<WhatsNewId> validator = StringValidatorBuilder
            .of("whatsNewId", c -> c.notBlank().greaterThanOrEqual(32).lessThanOrEqual(32))
            .build()
            .andThen(WhatsNewId::new);

    private final String value;

    private WhatsNewId(String value) {
        this.value = value;
    }

    public WhatsNewId() {
        this(NanoIdUtils.randomNanoId());
    }

    public static WhatsNewId of(String value) {
        return validator.validated(value);
    }

    public String asString() {
        return value;
    }

    @Override
    public String toString() {
        return asString();
    }

}
