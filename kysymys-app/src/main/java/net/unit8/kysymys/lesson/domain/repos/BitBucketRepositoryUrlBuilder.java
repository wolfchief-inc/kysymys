package net.unit8.kysymys.lesson.domain.repos;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;

public class BitBucketRepositoryUrlBuilder extends AbstractRepositoryUrlBuilder {
    public BitBucketRepositoryUrlBuilder(String url) {
        super(url);
    }

    @Override
    public URL build() {
        try {
            if (commitHash != null) {
                return new URL(chopDotGitSuffix(url) + "/src/" + commitHash);
            } else {
                return new URL(chopDotGitSuffix(url) + "/src/" + branch + path);
            }
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
    }
}
