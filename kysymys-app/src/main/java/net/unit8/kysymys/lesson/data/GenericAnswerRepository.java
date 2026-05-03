package net.unit8.kysymys.lesson.data;

import static net.unit8.kysymys.lesson.data.BranchNamePattern.validateUrl;

public record GenericAnswerRepository(String url) implements AnswerRepository {
    public GenericAnswerRepository { url = validateUrl(url); }

    @Override
    public String commitUrl(CommitHash hash) {
        return url;
    }
}
