package net.unit8.kysymys.user.domain;

import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.builder.StringValidatorBuilder;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class UserName {
    public static final StringValidator<String> validator = StringValidatorBuilder
            .of("userName", c -> c.notBlank().lessThanOrEqual(100))
            .build();

    private final String value;

    private UserName(String value) {
        this.value = value;
    }

    public static UserName of(String value) {
        return validator.andThen(UserName::new).validated(value);
    }

    public String asString() {
        return value;
    }

    @Override
    public String toString() {
        return asString();
    }
}
