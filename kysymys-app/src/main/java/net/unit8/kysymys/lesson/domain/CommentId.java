package net.unit8.kysymys.lesson.domain;

import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.builder.StringValidatorBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentId {
    public static final StringValidator<CommentId> validator = StringValidatorBuilder
            .of("commentId", c -> c.notBlank().greaterThanOrEqual(32).lessThanOrEqual(32))
            .build()
            .andThen(CommentId::new);

    String value;

    public static CommentId of(String value) {
        return validator.validated(value);
    }

    public String asString() {
        return asString();
    }

    @Override
    public String toString() {
        return asString();
    }
}
