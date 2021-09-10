package net.unit8.kysymys.lesson.domain;

import am.ik.yavi.arguments.Arguments2Validator;
import am.ik.yavi.arguments.ArgumentsValidators;
import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.builder.StringValidatorBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Value
public class AnswerRepository {
    private static StringValidator<String> urlValidator = StringValidatorBuilder.of("url", c -> c.url())
            .build();
    private static StringValidator<String> commitHashValidator = StringValidatorBuilder.of("commitHash", c -> c.url())
            .build();

    public static Arguments2Validator<String, String, AnswerRepository> validator = ArgumentsValidators
            .split(urlValidator, commitHashValidator)
            .apply(AnswerRepository::new);

    String url;
    String commitHash;

    public static AnswerRepository of(String url, String commitHash) {
        return validator.validated(url, commitHash);
    }
}
