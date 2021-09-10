package net.unit8.kysymys.lesson.domain;

import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.builder.StringValidatorBuilder;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import lombok.Value;

@Value
public class ProblemId {
    public static final StringValidator<ProblemId> validator = StringValidatorBuilder
            .of("value", c -> c.notBlank().greaterThanOrEqual(21).lessThanOrEqual(21))
            .build()
            .andThen(ProblemId::new);

    String value;

    private ProblemId(String value) {
        this.value = value;
    }

    public ProblemId() {
        this(NanoIdUtils.randomNanoId());
    }

    public static ProblemId of(String value) {
        return validator.validated(value);
    }
}
