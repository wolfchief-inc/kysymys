package net.unit8.kysymys.lesson.domain;

import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.builder.StringValidatorBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProblemName {
    public static final StringValidator<ProblemName> validator = StringValidatorBuilder
            .of("name", c -> c.notBlank().lessThanOrEqual(100))
            .build()
            .andThen(ProblemName::new);

    private final String value;

    public static ProblemName of(String value) {
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
