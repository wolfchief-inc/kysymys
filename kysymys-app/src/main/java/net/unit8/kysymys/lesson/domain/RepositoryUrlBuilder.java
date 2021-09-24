package net.unit8.kysymys.lesson.domain;

import net.unit8.kysymys.lesson.domain.repos.BitBucketRepositoryUrlBuilder;
import net.unit8.kysymys.lesson.domain.repos.GenericRepositoryUrlBuilder;
import net.unit8.kysymys.lesson.domain.repos.GitHubRepositoryUrlBuilder;

import java.net.URL;

public interface RepositoryUrlBuilder {
    static RepositoryUrlBuilder url(String url) {
        if (url == null) throw new IllegalArgumentException("url must be not-null");
        if (url.startsWith("https://github.com/")) {
            return new GitHubRepositoryUrlBuilder(url);
        } else if (url.startsWith("https://bitbucket.org")) {
            return new BitBucketRepositoryUrlBuilder(url);
        }
        return new GenericRepositoryUrlBuilder(url);
    }
    RepositoryUrlBuilder branch(String branch);
    RepositoryUrlBuilder commitHash(String commitHash);
    RepositoryUrlBuilder path(String branch);
    URL build();
}
