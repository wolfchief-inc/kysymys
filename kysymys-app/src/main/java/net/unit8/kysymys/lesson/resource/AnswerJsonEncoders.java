package net.unit8.kysymys.lesson.resource;

import net.unit8.kysymys.lesson.data.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class AnswerJsonEncoders {
    private AnswerJsonEncoders() {}

    public static Map<String, Object> encode(Answer a, Optional<Submission> latest) {
        return encode(a, latest, List.of());
    }

    public static Map<String, Object> encode(Answer a, Optional<Submission> latest,
                                             List<ReviewComment> comments) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", a.id().value());
        body.put("problemId", a.problemId().value());
        body.put("answererId", a.answererId().value());
        body.put("repository", encodeRepository(a.repository()));
        latest.ifPresent(s -> {
            body.put("latestCommitHash", s.commitHash().value());
            body.put("latestSubmittedAt", s.submittedAt().toString());
            body.put("answerUrl", a.repository().commitUrl(s.commitHash()));
        });
        body.put("comments", comments.stream().map(c -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.id().value());
            m.put("commenterId", c.commenterId().value());
            m.put("description", c.description().value());
            m.put("postedAt", c.postedAt().toString());
            return m;
        }).toList());
        return body;
    }

    private static Map<String, Object> encodeRepository(AnswerRepository repo) {
        Map<String, Object> m = new LinkedHashMap<>();
        switch (repo) {
            case GitHubAnswerRepository g -> { m.put("type", "github"); m.put("url", g.url()); }
            case BitBucketAnswerRepository b -> { m.put("type", "bitbucket"); m.put("url", b.url()); }
            case GenericAnswerRepository g -> { m.put("type", "generic"); m.put("url", g.url()); }
        }
        return m;
    }
}
