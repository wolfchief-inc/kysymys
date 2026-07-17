package net.unit8.kysymys.lesson.data;

import org.jspecify.annotations.Nullable;

import static net.unit8.kysymys.lesson.data.BranchNamePattern.validateBranch;
import static net.unit8.kysymys.lesson.data.BranchNamePattern.validateUrl;

public record GenericProblemRepository(String url, String branch)
        implements ProblemRepository {
    public GenericProblemRepository {
        url = validateUrl(url);
        branch = validateBranch(branch);
    }

    /** A generic repository exposes no readme path. */
    @Override
    public @Nullable String readmePath() {
        return null;
    }

    @Override
    public String typeKey() {
        return "generic";
    }

    @Override
    public String problemUrl() {
        return url;
    }
}
