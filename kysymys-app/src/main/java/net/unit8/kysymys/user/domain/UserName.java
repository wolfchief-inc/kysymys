package net.unit8.kysymys.user.domain;

import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.builder.StringValidatorBuilder;
import lombok.Value;

@Value
public class UserName {
    public static final StringValidator<String> validator = StringValidatorBuilder
            .of("value", c -> c.notBlank().lessThanOrEqual(100))
            .build();

    String value;

    private UserName(String value) {
        this.value = value;
    }

    public static UserName of(String value) {
        return validator.andThen(UserName::new).validated(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
