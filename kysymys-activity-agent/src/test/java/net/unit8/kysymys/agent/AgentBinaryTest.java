package net.unit8.kysymys.agent;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AgentBinaryTest {

    @Test
    void mapsShippedPlatforms() {
        assertThat(AgentBinary.osArchTag("Mac OS X", "aarch64")).isEqualTo("darwin-arm64");
        assertThat(AgentBinary.osArchTag("Windows 11", "amd64")).isEqualTo("windows-amd64");
        assertThat(AgentBinary.osArchTag("Linux", "x86_64")).isEqualTo("linux-amd64");
    }

    @Test
    void returnsNullForUnshippedPlatforms() {
        assertThat(AgentBinary.osArchTag("Mac OS X", "x86_64")).isNull();   // mac Intel not shipped
        assertThat(AgentBinary.osArchTag("Linux", "aarch64")).isNull();     // linux arm not shipped
        assertThat(AgentBinary.osArchTag("SunOS", "sparc")).isNull();
        assertThat(AgentBinary.osArchTag(null, "amd64")).isNull();
    }
}
