package net.unit8.kysymys.lesson.resource;

import net.unit8.kysymys.lesson.data.*;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ProblemJsonEncoders {
    private ProblemJsonEncoders() {}

    public static Map<String, Object> encode(Problem p, ProblemStatus status) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", p.id().value());
        body.put("name", p.name().value());
        body.put("repository", encodeRepository(p.repository()));
        body.put("status", status.name());
        body.put("problemUrl", p.repository().problemUrl());
        return body;
    }

    private static Map<String, Object> encodeRepository(ProblemRepository repo) {
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
}
