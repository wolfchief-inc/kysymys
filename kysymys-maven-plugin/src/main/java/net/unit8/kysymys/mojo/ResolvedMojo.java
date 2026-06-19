package net.unit8.kysymys.mojo;

import org.apache.maven.plugins.annotations.Mojo;

/**
 * {@code mvn kysymys:resolved} — clears a previously raised "stuck" signal once
 * the participant is unblocked.
 */
@Mojo(name = "resolved", requiresProject = false)
public class ResolvedMojo extends AbstractActivityMojo {
    @Override
    protected String kind() {
        return "RESOLVED";
    }
}
