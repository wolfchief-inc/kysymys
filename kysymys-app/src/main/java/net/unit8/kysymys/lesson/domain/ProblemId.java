package net.unit8.kysymys.lesson.domain;

import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.builder.StringValidatorBuilder;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class ProblemId {
    public static final StringValidator<ProblemId> validator = StringValidatorBuilder
            .of("value", c -> c.notBlank().greaterThanOrEqual(21).lessThanOrEqual(21))
            .build()
            .andThen(ProblemId::new);

    private final String value;

    private ProblemId(String value) {
        this.value = value;
    }

    public ProblemId() {
        this(NanoIdUtils.randomNanoId());
    }

    public static ProblemId of(String value) {
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
