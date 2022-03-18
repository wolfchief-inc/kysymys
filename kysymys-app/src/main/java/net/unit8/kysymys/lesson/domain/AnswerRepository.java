package net.unit8.kysymys.lesson.domain;

import am.ik.yavi.arguments.Arguments2Validator;
import am.ik.yavi.arguments.ArgumentsValidators;
import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.builder.StringValidatorBuilder;
import lombok.*;

@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AnswerRepository {
    private final static StringValidator<String> urlValidator = StringValidatorBuilder.of("url", c -> c.url())
            .build();
    private final static StringValidator<String> commitHashValidator = StringValidatorBuilder.of("commitHash", c ->
            c.lessThanOrEqual(40).greaterThanOrEqual(40))
            .build();

    public static Arguments2Validator<String, String, AnswerRepository> validator = ArgumentsValidators
            .split(urlValidator, commitHashValidator)
            .apply(AnswerRepository::new);

    @Getter
    private final String url;
    @Getter
    private final String commitHash;

    public static AnswerRepository of(String url, String commitHash) {
        return validator.validated(url, commitHash);
    }
}
