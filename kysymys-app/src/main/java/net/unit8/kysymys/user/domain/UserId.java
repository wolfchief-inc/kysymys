package net.unit8.kysymys.user.domain;

import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.builder.StringValidatorBuilder;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import lombok.EqualsAndHashCode;
import lombok.Value;

@EqualsAndHashCode
public class UserId {
    public static final StringValidator<UserId> validator = StringValidatorBuilder
            .of("userId", c -> c.notBlank().greaterThanOrEqual(21).lessThanOrEqual(21))
            .build()
            .andThen(UserId::new);

    private final String value;

    private UserId(String value) {
        this.value = value;
    }

    public UserId() {
        this(NanoIdUtils.randomNanoId());
    }

    public static UserId of(String value) {
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
