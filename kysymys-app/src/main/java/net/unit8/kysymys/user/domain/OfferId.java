package net.unit8.kysymys.user.domain;

import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.builder.StringValidatorBuilder;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import lombok.Value;

@Value
public class OfferId {
    public static final StringValidator<OfferId> validator = StringValidatorBuilder
            .of("value", c -> c.notBlank().greaterThanOrEqual(21).lessThanOrEqual(21))
            .build()
            .andThen(OfferId::new);

    String value;

    private OfferId(String value) {
        this.value = value;
    }

    public OfferId() {
        this(NanoIdUtils.randomNanoId());
    }

    public static OfferId of(String value) {
        return validator.validated(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
