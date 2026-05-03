package net.unit8.kysymys.lesson.data;

import static net.unit8.kysymys.lesson.data.BranchNamePattern.chopDotGit;
import static net.unit8.kysymys.lesson.data.BranchNamePattern.validateUrl;

public record BitBucketAnswerRepository(String url) implements AnswerRepository {
    public BitBucketAnswerRepository { url = validateUrl(url); }

    @Override
    public String commitUrl(CommitHash hash) {
        return chopDotGit(url) + "/commits/" + hash.value();
    }
}
