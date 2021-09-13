package net.unit8.kysymys.lesson.domain;

import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.builder.StringValidatorBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Description {
    public static final StringValidator<Description> validator = StringValidatorBuilder
            .of("value", c -> c.notBlank().lessThanOrEqual(100).email())
            .build()
            .andThen(Description::new);
    String value;

    public static Description of(String value) {
        return validator.validated(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
