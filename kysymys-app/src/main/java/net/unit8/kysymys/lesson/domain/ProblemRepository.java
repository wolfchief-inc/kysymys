package net.unit8.kysymys.lesson.domain;

import am.ik.yavi.arguments.Arguments3Validator;
import am.ik.yavi.arguments.ArgumentsValidators;
import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.builder.StringValidatorBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.net.URL;

@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProblemRepository {
    private final static StringValidator<String> urlValidator = StringValidatorBuilder
            .of("url", c -> c.notNull().url().lessThanOrEqual(100))
            .build();
    private final static StringValidator<String> branchValidator = StringValidatorBuilder
            .of("branch", c -> c.notNull().lessThanOrEqual(100))
            .build();
    private final static StringValidator<String> readmePathValidator = StringValidatorBuilder
            .of("readmePath", c -> c.notNull().lessThanOrEqual(100))
            .build();

    public static Arguments3Validator<String, String, String, ProblemRepository> validator = ArgumentsValidators
            .split(urlValidator, branchValidator, readmePathValidator)
            .apply(ProblemRepository::new);

    @Getter
    private final String url;
    @Getter
    private final String branch;
    @Getter
    private final String readmePath;

    public static ProblemRepository of(String url, String branch, String readmePath) {
        return validator.validated(url, branch, readmePath);
    }

    public static ProblemRepository of(String url, String branch) {
        return of(url, branch, "/README.md");
    }

    public URL getProblemUrl() {
        return RepositoryUrlBuilder.url(url)
                .branch(branch)
                .path(readmePath)
                .build();
    }
}
