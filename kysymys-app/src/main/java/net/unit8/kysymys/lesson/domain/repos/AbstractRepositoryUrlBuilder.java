package net.unit8.kysymys.lesson.domain.repos;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.Validator;
import net.unit8.kysymys.lesson.domain.RepositoryUrlBuilder;

abstract class AbstractRepositoryUrlBuilder implements RepositoryUrlBuilder {
    // see https://www.spinics.net/lists/git/msg133704.html
    private static final String BRANCH_PTN = "^(?!(^\\.|.*(\\.\\.|\\p{Space}|\\p{Cntrl}))).*(?<!(/|.lock))$";

    private final static Validator<AbstractRepositoryUrlBuilder> validator = ValidatorBuilder.of(AbstractRepositoryUrlBuilder.class)
            .constraintOnTarget(b -> (b.branch != null && b.commitHash == null) || (b.branch == null &&  b.commitHash != null),
                    "branchOrCommitHashIsRequired", "", "branch or commit hash is required")
            .constraint((ValidatorBuilder.ToCharSequence<AbstractRepositoryUrlBuilder, String>) b -> b.branch,
                    "branch", c-> c.pattern(BRANCH_PTN).lessThanOrEqual(100))
            .constraint((ValidatorBuilder.ToCharSequence<AbstractRepositoryUrlBuilder, String>) b -> b.commitHash,
                    "commitHash", c -> c.lessThanOrEqual(40).greaterThanOrEqual(40).pattern("^[0-9a-fA-F]{40}$"))
            .constraint((ValidatorBuilder.ToCharSequence<AbstractRepositoryUrlBuilder, String>) b -> b.path,
                    "path", c -> c.lessThanOrEqual(100))
            .build();
    protected final String url;
    protected String commitHash;
    protected String branch;
    protected String path;

    public String getBranch() {
        return branch;
    }
    public AbstractRepositoryUrlBuilder(String url) {
        this.url = url;
    }

    @Override
    public RepositoryUrlBuilder branch(String branch) {
        this.branch = branch;
        return this;
    }

    @Override
    public RepositoryUrlBuilder commitHash(String commitHash) {
        this.commitHash = commitHash;
        return this;
    }

    @Override
    public RepositoryUrlBuilder path(String path) {
        this.path = path;
        return this;
    }

    protected void validate() {
        validator.applicative().validated(this);
    }

    protected String chopDotGitSuffix(String url) {
        return url.endsWith(".git") ? url.substring(0, url.length() - 4): url;
    }
}
