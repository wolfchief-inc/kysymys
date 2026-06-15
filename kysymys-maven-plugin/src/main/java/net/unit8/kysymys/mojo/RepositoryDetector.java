package net.unit8.kysymys.mojo;

import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

public class RepositoryDetector {
    /**
     * Normalizes an http(s) remote URL by dropping any userinfo/fragment. SSH/SCP-like remotes
     * (e.g. {@code git@github.com:owner/repo.git}, {@code ssh://git@host/...}) are not valid
     * {@link URL}s and are returned unchanged.
     */
    static String normalizeUrl(String urlStr) throws MalformedURLException {
        if (urlStr == null) {
            return null;
        }
        if (urlStr.startsWith("http://") || urlStr.startsWith("https://")) {
            URL url = new URL(urlStr);
            return new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getFile())
                    .toExternalForm();
        }
        return urlStr;
    }

    public RepositoryDetectionResult detect() throws IOException {
        Repository repository = new FileRepositoryBuilder()
                .readEnvironment()
                .findGitDir()
                .build();

        Ref headRef = repository.findRef(repository.getFullBranch());
        String commitHash = headRef.getObjectId().name();
        Set<String> remoteNames = repository.getRemoteNames();
        String remoteName = remoteNames.contains("origin") ? "origin" : remoteNames.stream().findAny().orElseThrow();
        String remoteUrl = repository.getConfig().getString("remote", remoteName, "url");
        return new RepositoryDetectionResult(normalizeUrl(remoteUrl), commitHash);
    }
}
