package net.unit8.kysymys.lesson.domain;

import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.builder.StringValidatorBuilder;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import lombok.EqualsAndHashCode;
import lombok.Value;

@EqualsAndHashCode
public class AnswerId {
    public static final StringValidator<AnswerId> validator = StringValidatorBuilder
            .of("answerId", c -> c.notBlank().greaterThanOrEqual(21).lessThanOrEqual(21))
            .build()
            .andThen(AnswerId::new);

    private final String value;

    private AnswerId(String value) {
        this.value = value;
    }

    public AnswerId() {
        this(NanoIdUtils.randomNanoId());
    }

    public static AnswerId of(String value) {
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
