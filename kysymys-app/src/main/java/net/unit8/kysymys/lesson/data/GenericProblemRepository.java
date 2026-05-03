package net.unit8.kysymys.lesson.data;

import static net.unit8.kysymys.lesson.data.BranchNamePattern.validateBranch;
import static net.unit8.kysymys.lesson.data.BranchNamePattern.validateUrl;

public record GenericProblemRepository(String url, String branch)
        implements ProblemRepository {
    public GenericProblemRepository {
        url = validateUrl(url);
        branch = validateBranch(branch);
    }

    @Override
    public String problemUrl() {
        return url;
    }
}
