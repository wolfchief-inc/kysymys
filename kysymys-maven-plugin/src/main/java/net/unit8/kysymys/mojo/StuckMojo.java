package net.unit8.kysymys.mojo;

import org.apache.maven.plugins.annotations.Mojo;

/**
 * {@code mvn kysymys:stuck} — the participant's explicit "I need help" signal.
 * Surfaces them on the instructor dashboard until they run
 * {@code mvn kysymys:resolved}.
 */
@Mojo(name = "stuck", requiresProject = false)
public class StuckMojo extends AbstractActivityMojo {
    @Override
    protected String kind() {
        return "STUCK";
    }
}
