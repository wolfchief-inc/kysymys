package net.unit8.kysymys.user.domain;

import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.builder.StringValidatorBuilder;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import lombok.EqualsAndHashCode;
import lombok.Value;

@EqualsAndHashCode
public class OfferId {
    public static final StringValidator<OfferId> validator = StringValidatorBuilder
            .of("offerId", c -> c.notBlank().greaterThanOrEqual(21).lessThanOrEqual(21))
            .build()
            .andThen(OfferId::new);

    private final String value;

    private OfferId(String value) {
        this.value = value;
    }

    public OfferId() {
        this(NanoIdUtils.randomNanoId());
    }

    public static OfferId of(String value) {
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
