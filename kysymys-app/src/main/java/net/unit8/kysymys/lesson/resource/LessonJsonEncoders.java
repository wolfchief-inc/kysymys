package net.unit8.kysymys.lesson.resource;

import net.unit8.kysymys.lesson.data.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * JSON response shapes for the Lesson context.
 */
public final class LessonJsonEncoders {
    private LessonJsonEncoders() {}

    public static Map<String, Object> encodeProblem(Problem p, ProblemStatus status) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", p.id().value());
        body.put("name", p.name().value());
        body.put("repository", encodeProblemRepository(p.repository()));
        body.put("status", status.name());
        body.put("problemUrl", p.repository().problemUrl());
        return body;
    }

    public static Map<String, Object> encodeAnswer(Answer a, Optional<Submission> latest) {
        return encodeAnswer(a, latest, List.of());
    }

    public static Map<String, Object> encodeAnswer(Answer a, Optional<Submission> latest,
                                                   List<ReviewComment> comments) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", a.id().value());
        body.put("problemId", a.problemId().value());
        body.put("answererId", a.answererId().value());
        body.put("repository", encodeAnswerRepository(a.repository()));
        latest.ifPresent(s -> {
            body.put("latestCommitHash", s.commitHash().value());
            body.put("latestSubmittedAt", s.submittedAt().toString());
            body.put("answerUrl", a.repository().commitUrl(s.commitHash()));
        });
        body.put("comments", comments.stream().map(LessonJsonEncoders::encodeComment).toList());
        return body;
    }

    public static Map<String, Object> encodeComment(ReviewComment c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.id().value());
        m.put("answerId", c.answerId().value());
        m.put("commenterId", c.commenterId().value());
        m.put("description", c.description().value());
        m.put("postedAt", c.postedAt().toString());
        return m;
    }

    private static Map<String, Object> encodeProblemRepository(ProblemRepository repo) {
        Map<String, Object> m = new LinkedHashMap<>();
        switch (repo) {
            case GitHubProblemRepository g -> {
                m.put("type", "github");
                m.put("url", g.url());
                m.put("branch", g.branch());
                m.put("readmePath", g.readmePath());
            }
            case BitBucketProblemRepository b -> {
                m.put("type", "bitbucket");
                m.put("url", b.url());
                m.put("branch", b.branch());
                m.put("readmePath", b.readmePath());
            }
            case GenericProblemRepository g -> {
                m.put("type", "generic");
                m.put("url", g.url());
                m.put("branch", g.branch());
            }
        }
        return m;
    }

    private static Map<String, Object> encodeAnswerRepository(AnswerRepository repo) {
        Map<String, Object> m = new LinkedHashMap<>();
        switch (repo) {
            case GitHubAnswerRepository g -> { m.put("type", "github"); m.put("url", g.url()); }
            case BitBucketAnswerRepository b -> { m.put("type", "bitbucket"); m.put("url", b.url()); }
            case GenericAnswerRepository g -> { m.put("type", "generic"); m.put("url", g.url()); }
        }
        return m;
    }
}
