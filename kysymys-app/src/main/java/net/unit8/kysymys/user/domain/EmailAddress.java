package net.unit8.kysymys.user.domain;

import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.builder.StringValidatorBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EmailAddress {
    public static final StringValidator<String> validator = StringValidatorBuilder
            .of("value", c -> c.notBlank().lessThanOrEqual(100).email())
            .build();

    String value;

    public static EmailAddress of(String value) {
        return validator.andThen(EmailAddress::new).validated(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
