package net.unit8.kysymys.lesson.data;

import static net.unit8.kysymys.lesson.data.BranchNamePattern.chopDotGit;
import static net.unit8.kysymys.lesson.data.BranchNamePattern.validateBranch;
import static net.unit8.kysymys.lesson.data.BranchNamePattern.validateReadmePath;
import static net.unit8.kysymys.lesson.data.BranchNamePattern.validateUrl;

public record GitHubProblemRepository(String url, String branch, String readmePath)
        implements ProblemRepository {
    public GitHubProblemRepository {
        url = validateUrl(url);
        branch = validateBranch(branch);
        readmePath = validateReadmePath(readmePath);
    }

    @Override
    public String typeKey() {
        return "github";
    }

    @Override
    public String problemUrl() {
        return chopDotGit(url) + "/blob/" + branch + readmePath;
    }
}
