package net.unit8.kysymys.lesson.domain;

import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.builder.StringValidatorBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Description {
    public static final StringValidator<Description> validator = StringValidatorBuilder
            .of("description", c -> c.notBlank().lessThanOrEqual(4000))
            .build()
            .andThen(Description::new);

    private final String value;

    public static Description of(String value) {
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
