package net.unit8.kysymys.mojo;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class RepositoryDetectorTest {
    @Test
    void test() throws IOException, GitAPIException {
        new RepositoryDetector().detect();
    }
}