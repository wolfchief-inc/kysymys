package net.unit8.kysymys.lesson.domain;

import am.ik.yavi.arguments.Arguments3Validator;
import am.ik.yavi.arguments.ArgumentsValidators;
import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.builder.StringValidatorBuilder;
import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.Validator;
import am.ik.yavi.fn.Validations;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.net.URI;
import java.net.URL;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Value
public class ProblemRepository {
    private static StringValidator<String> urlValidator = StringValidatorBuilder
            .of("url", c -> c.notNull().url().lessThanOrEqual(100))
            .build();
    private static StringValidator<String> branchValidator = StringValidatorBuilder
            .of("branch", c -> c.notNull().lessThanOrEqual(100))
            .build();
    private static StringValidator<String> readmePathValidator = StringValidatorBuilder
            .of("readmePath", c -> c.notNull().lessThanOrEqual(100))
            .build();

    public static Arguments3Validator<String, String, String, ProblemRepository> validator = ArgumentsValidators
            .split(urlValidator, branchValidator, readmePathValidator)
            .apply(ProblemRepository::new);

    String url;
    String branch;
    String readmePath;

    public static ProblemRepository of(String url, String branch, String readmePath) {
        return validator.validated(url, branch, readmePath);
    }

    public static ProblemRepository of(String url, String branch) {
        return of(url, branch, "/README.md");
    }

    public URI getProblemUrl() {
        if (url.startsWith("https://github.com/")) {
            String githubUrl = url.endsWith(".git") ? url.substring(0, url.length() - 4): url;
            return URI.create(githubUrl + "/blob/" + branch + readmePath);
        }
        return URI.create(url);
    }
}
