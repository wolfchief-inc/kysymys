package net.unit8.kysymys.lesson.domain;

import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.builder.StringValidatorBuilder;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import lombok.Value;

@Value
public class CommentId {
    public static final StringValidator<CommentId> validator = StringValidatorBuilder
            .of("value", c -> c.notBlank().greaterThanOrEqual(32).lessThanOrEqual(32))
            .build()
            .andThen(CommentId::new);

    String value;

    private CommentId(String value) {
        this.value = value;
    }

    public static CommentId of(String value) {
        return validator.validated(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
