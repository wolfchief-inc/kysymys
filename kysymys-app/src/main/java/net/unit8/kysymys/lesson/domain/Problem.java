package net.unit8.kysymys.lesson.domain;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.Arguments2;
import am.ik.yavi.arguments.Arguments3;
import am.ik.yavi.arguments.Arguments3Validator;
import am.ik.yavi.builder.ArgumentsValidatorBuilder;
import lombok.Value;

@Value
public class Problem {
    public static final Arguments3Validator<ProblemId, ProblemName, ProblemRepository, Problem> validator = ArgumentsValidatorBuilder.of(Problem::new)
            .builder(b -> b._object(Arguments1::arg1, "id", c -> c.notNull()))
            .builder(b -> b._object(Arguments2::arg2, "name", c -> c.notNull()))
            .builder(b -> b._object(Arguments3::arg3, "repository", c -> c.notNull()))
            .build();

    ProblemId id;
    ProblemName name;
    ProblemRepository repository;

    public static Problem of(ProblemId problemId, ProblemName name, ProblemRepository repository) {
        return validator.validated(problemId, name, repository);
    }
}
