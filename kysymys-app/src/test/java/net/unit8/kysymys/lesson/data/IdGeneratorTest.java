package net.unit8.kysymys.lesson.data;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IdGeneratorTest {
    @Test
    void generates21CharNanoid() {
        String id = IdGenerator.newId();
        assertThat(id).hasSize(21);
        assertThat(id).matches("[A-Za-z0-9_-]{21}");
    }

    @Test
    void generatesUniqueIds() {
        String a = IdGenerator.newId();
        String b = IdGenerator.newId();
        assertThat(a).isNotEqualTo(b);
    }
}
