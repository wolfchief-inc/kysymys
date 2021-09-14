package net.unit8.kysymys.lesson.domain;

import am.ik.yavi.arguments.Arguments;
import am.ik.yavi.arguments.Arguments4;
import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.ValueValidator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import net.unit8.kysymys.user.domain.UserId;

import java.net.URL;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Value
public class Answer {
    public static final ValueValidator<Arguments4<AnswerId, Problem, UserId, AnswerRepository>, Answer> validator = ValidatorBuilder.of(Answer.class)
            .build()
            .applicative()
            .compose(args -> new Answer(args.arg1(), args.arg2(), args.arg3(), args.arg4()));

    AnswerId id;
    Problem problem;
    UserId answererId;
    AnswerRepository repository;

    public static Answer of(AnswerId id, Problem problem, UserId answererId, AnswerRepository repository) {
        return validator.validated(Arguments.of(id, problem, answererId, repository));
    }

    public URL getAnswerUrl() {
        return RepositoryUrlBuilder.url(repository.getUrl())
                .commitHash(repository.getCommitHash())
                .build();
    }
}
