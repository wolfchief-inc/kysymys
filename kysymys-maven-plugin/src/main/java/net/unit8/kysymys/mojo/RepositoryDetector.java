package net.unit8.kysymys.mojo;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;

public class RepositoryDetector {
    public void detect() throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = builder.setGitDir(new File("."))
                .readEnvironment()
                .findGitDir()
                .build();

        RevWalk walk = new RevWalk(repository);
        ObjectId head = repository.resolve("HEAD");
        RevCommit commit = walk.parseCommit(head);
    }
}
