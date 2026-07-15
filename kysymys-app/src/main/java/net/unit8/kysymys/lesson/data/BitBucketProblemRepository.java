package net.unit8.kysymys.lesson.data;

import static net.unit8.kysymys.lesson.data.BranchNamePattern.chopDotGit;
import static net.unit8.kysymys.lesson.data.BranchNamePattern.validateBranch;
import static net.unit8.kysymys.lesson.data.BranchNamePattern.validateReadmePath;
import static net.unit8.kysymys.lesson.data.BranchNamePattern.validateUrl;

public record BitBucketProblemRepository(String url, String branch, String readmePath)
        implements ProblemRepository {
    public BitBucketProblemRepository {
        url = validateUrl(url);
        branch = validateBranch(branch);
        readmePath = validateReadmePath(readmePath);
    }

    @Override
    public String typeKey() {
        return "bitbucket";
    }

    @Override
    public String problemUrl() {
        return chopDotGit(url) + "/src/" + branch + readmePath;
    }
}
