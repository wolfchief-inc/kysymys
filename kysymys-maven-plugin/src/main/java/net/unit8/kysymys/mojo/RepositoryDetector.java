package net.unit8.kysymys.mojo;

import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.IOException;
import java.util.Set;

public class RepositoryDetector {
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
        return new RepositoryDetectionResult(remoteUrl, commitHash);
    }
}
