package net.unit8.kysymys.share.adapter.system;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FlakeAdapterTest {
    private static final int ITER = 10000;
    @Test
    void test() {
        String[] ids = new String[ITER];
        FlakeAdapter flake = new FlakeAdapter();
        for (int i=0; i<ITER; i++) {
            ids[i] = flake.generateId();
        }

        for (int i=1; i<ITER; i++) {
            assertThat(ids[i-1]).isLessThan(ids[i]);
        }
    }
}