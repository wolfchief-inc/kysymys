package net.unit8.kysymys.notification.domain;

import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.builder.StringValidatorBuilder;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import lombok.Value;

@Value
public class WhatsNewId {
    public static final StringValidator<WhatsNewId> validator = StringValidatorBuilder
            .of("value", c -> c.notBlank().greaterThanOrEqual(32).lessThanOrEqual(32))
            .build()
            .andThen(WhatsNewId::new);

    String value;

    private WhatsNewId(String value) {
        this.value = value;
    }

    public WhatsNewId() {
        this(NanoIdUtils.randomNanoId());
    }

    public static WhatsNewId of(String value) {
        return validator.validated(value);
    }

    @Override
    public String toString() {
        return value;
    }

}
