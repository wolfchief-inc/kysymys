package net.unit8.kysymys.lesson.domain.repos;

import net.unit8.kysymys.lesson.domain.RepositoryUrlBuilder;

abstract class AbstractRepositoryUrlBuilder implements RepositoryUrlBuilder {
    protected final String url;
    protected String commitHash;
    protected String branch;
    protected String path;

    public AbstractRepositoryUrlBuilder(String url) {
        this.url = url;
    }

    @Override
    public RepositoryUrlBuilder branch(String branch) {
        this.branch = branch;
        return this;
    }

    @Override
    public RepositoryUrlBuilder commitHash(String branch) {
        this.commitHash = commitHash;
        return this;
    }

    @Override
    public RepositoryUrlBuilder path(String path) {
        this.path = path;
        return this;
    }

    protected String chopDotGitSuffix(String url) {
        return url.endsWith(".git") ? url.substring(0, url.length() - 4): url;
    }
}
