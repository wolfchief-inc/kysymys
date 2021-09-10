package net.unit8.kysymys.lesson.domain;

import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.builder.StringValidatorBuilder;
import lombok.Value;

@Value
public class ProblemName {
    public static final StringValidator<ProblemName> validator = StringValidatorBuilder
            .of("value", c -> c.notBlank().lessThanOrEqual(100))
            .build()
            .andThen(ProblemName::new);

    String value;

    private ProblemName(String value) {
        this.value = value;
    }

    public static ProblemName of(String value) {
        return validator.validated(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
