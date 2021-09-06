package net.unit8.kysymys.user.domain;

import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.builder.StringValidatorBuilder;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import lombok.Value;

@Value
public class UserId {
    public static final StringValidator<String> validator = StringValidatorBuilder
            .of("value", c -> c.notBlank().greaterThanOrEqual(21).lessThanOrEqual(21))
            .build();

    String value;

    private UserId(String value) {
        this.value = value;
    }

    public UserId() {
        this(validator.validated(NanoIdUtils.randomNanoId()));
    }

    public static UserId of(String value) {
        return validator.andThen(UserId::new).validated(value);
    }
}
