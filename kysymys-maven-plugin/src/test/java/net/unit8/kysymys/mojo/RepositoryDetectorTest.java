package net.unit8.kysymys.mojo;

import org.junit.jupiter.api.Test;

import java.io.IOException;

class RepositoryDetectorTest {
    @Test
    void test() throws IOException {
        new RepositoryDetector().detect();
    }
}