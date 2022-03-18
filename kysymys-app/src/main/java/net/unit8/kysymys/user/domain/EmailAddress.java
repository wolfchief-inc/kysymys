package net.unit8.kysymys.user.domain;

import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.builder.StringValidatorBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EmailAddress {
    public static final StringValidator<String> validator = StringValidatorBuilder
            .of("emailAddress", c -> c.notBlank().lessThanOrEqual(100).email())
            .build();

    private final String value;

    public static EmailAddress of(String value) {
        return validator.andThen(EmailAddress::new).validated(value);
    }

    public String asString() {
        return value;
    }

    @Override
    public String toString() {
        return asString();
    }
}
