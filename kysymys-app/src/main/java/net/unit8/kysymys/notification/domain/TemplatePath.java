package net.unit8.kysymys.notification.domain;

import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.builder.StringValidatorBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TemplatePath {
    public static final StringValidator<TemplatePath> validator = StringValidatorBuilder
            .of("templatePath", c -> c.notBlank().lessThanOrEqual(4000))
            .build()
            .andThen(TemplatePath::new);

    private String value;

    public static TemplatePath of(String value) {
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
