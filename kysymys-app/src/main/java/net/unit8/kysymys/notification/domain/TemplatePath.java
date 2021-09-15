package net.unit8.kysymys.notification.domain;

import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.builder.StringValidatorBuilder;
import lombok.Value;

@Value
public class TemplatePath {
    public static final StringValidator<TemplatePath> validator = StringValidatorBuilder
            .of("value", c -> c.notBlank().lessThanOrEqual(4000))
            .build()
            .andThen(TemplatePath::new);
    String value;

    public static TemplatePath of(String value) {
        return validator.validated(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
