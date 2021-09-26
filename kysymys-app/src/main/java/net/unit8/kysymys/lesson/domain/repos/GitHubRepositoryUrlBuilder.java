package net.unit8.kysymys.lesson.domain.repos;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

public class GitHubRepositoryUrlBuilder extends AbstractRepositoryUrlBuilder {

    public GitHubRepositoryUrlBuilder(String url) {
        super(url);
    }

    @Override
    public URL build() {
        validate();
        try {
            if (commitHash != null) {
                return new URL(chopDotGitSuffix(url) + "/tree/" + commitHash + Objects.requireNonNullElse(path, ""));
            } else if (branch != null){
                return new URL(chopDotGitSuffix(url) + "/blob/" + branch + Objects.requireNonNullElse(path, ""));
            } else {
                // Unreachable
                throw new IllegalStateException();
            }
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
    }
}
