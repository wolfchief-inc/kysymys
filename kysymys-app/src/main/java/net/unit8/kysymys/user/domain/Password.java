package net.unit8.kysymys.user.domain;

import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.builder.StringValidatorBuilder;
import lombok.*;

@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Password {
    public static final StringValidator<String> validator = StringValidatorBuilder
            .of("password", c -> c.notBlank().lessThanOrEqual(100))
            .build();

    @Getter
    private final String value;

    @Getter
    private final boolean encoded;

    public static Password ofEncoded(String value) {
        return validator.andThen(s -> new Password(s, true)).validated(value);
    }

    public static Password ofRaw(String value) {
        return validator.andThen(s -> new Password(s, false)).validated(value);
    }

    public static Password notSet() {
        return new Password(null, true);
    }
}
