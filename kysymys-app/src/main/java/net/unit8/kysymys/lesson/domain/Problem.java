package net.unit8.kysymys.lesson.domain;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.Arguments2;
import am.ik.yavi.arguments.Arguments3;
import am.ik.yavi.arguments.Arguments3Validator;
import am.ik.yavi.builder.ArgumentsValidatorBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.net.URL;

@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Problem {
    public static final Arguments3Validator<ProblemId, ProblemName, ProblemRepository, Problem> validator = ArgumentsValidatorBuilder.of(Problem::new)
            .builder(b -> b._object(Arguments1::arg1, "id", c -> c.notNull()))
            .builder(b -> b._object(Arguments2::arg2, "name", c -> c.notNull()))
            .builder(b -> b._object(Arguments3::arg3, "repository", c -> c.notNull()))
            .build();

    @Getter
    private final ProblemId id;
    @Getter
    private final ProblemName name;
    @Getter
    private final ProblemRepository repository;

    public static Problem of(ProblemId problemId, ProblemName name, ProblemRepository repository) {
        return validator.validated(problemId, name, repository);
    }

    public URL getProblemUrl() {
        return repository.getProblemUrl();
    }
}
