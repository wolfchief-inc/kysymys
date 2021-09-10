package net.unit8.kysymys.user.domain;

import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.builder.StringValidatorBuilder;

public class EmailAddress {
    public static final StringValidator<String> validator = StringValidatorBuilder
            .of("value", c -> c.notBlank().lessThanOrEqual(100).email())
            .build();

    String value;

    private EmailAddress(String value) {
        this.value = value;
    }

    public static EmailAddress of(String value) {
        return validator.andThen(EmailAddress::new).validated(value);
    }

}
