package net.unit8.kysymys.agent;

import org.apache.maven.eventspy.AbstractEventSpy;
import org.apache.maven.execution.ExecutionEvent;

import javax.inject.Named;
import javax.inject.Singleton;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Maven core extension that reports build outcomes to a kysymys server and keeps
 * a background {@link Watcher} alive so the instructor dashboard sees activity
 * even between builds. The participant enables it by dropping a
 * {@code .mvn/extensions.xml} into the exercise repository:
 *
 * <pre>{@code
 * <extensions>
 *   <extension>
 *     <groupId>net.unit8.kysymys</groupId>
 *     <artifactId>kysymys-activity-agent</artifactId>
 *     <version>0.2.0-SNAPSHOT</version>
 *   </extension>
 * </extensions>
 * }</pre>
 *
 * <p>It emits {@code BUILD_SUCCESS} / {@code BUILD_FAILURE} (with the first error
 * line) at the end of every {@code mvn} run. All work is best-effort: nothing
 * here may slow down or break the build.
 */
@Named("kysymys-activity")
@Singleton
public class KysymysActivityEventSpy extends AbstractEventSpy {

    private AgentConfig config;
    private ActivityClient client;
    private volatile String failureDetail;

    @Override
    @SuppressWarnings("unchecked")
    public void init(Context context) {
        try {
            Map<String, Object> data = context.getData();
            Map<String, String> userProps = (Map<String, String>) data.get("userProperties");
            Map<String, String> sysProps = (Map<String, String>) data.get("systemProperties");
            String wd = (String) data.get("workingDirectory");
            Path workingDir = wd != null ? Path.of(wd) : Path.of(".").toAbsolutePath().normalize();

            this.config = AgentConfig.resolve(userProps, sysProps, workingDir);
            if (config.isComplete()) {
                this.client = new ActivityClient(config.url(), config.token());
            } else {
                System.out.println("[kysymys-activity] kysymys.token not set; "
                        + "activity reporting disabled for this build.");
            }
        } catch (Exception e) {
            // Disabled rather than fatal — a telemetry agent must never break Maven.
            this.config = null;
            this.client = null;
        }
    }

    @Override
    public void onEvent(Object event) {
        if (client == null || !(event instanceof ExecutionEvent ee)) {
            return;
        }
        try {
            switch (ee.getType()) {
                case SessionStarted -> {
                    failureDetail = null;
                    ensureWatcher();
                }
                case MojoFailed, ProjectFailed, ForkFailed -> {
                    if (failureDetail == null) {
                        failureDetail = describeFailure(ee);
                    }
                }
                case SessionEnded -> {
                    if (failureDetail != null) {
                        client.post("BUILD_FAILURE", config.problemId(), failureDetail);
                    } else {
                        client.post("BUILD_SUCCESS", config.problemId(), null);
                    }
                }
                default -> { /* other lifecycle events carry no signal we need */ }
            }
        } catch (Exception ignored) {
            // best-effort
        }
    }

    private static String describeFailure(ExecutionEvent ee) {
        if (ee.getException() != null && ee.getException().getMessage() != null) {
            return stripAnsi(ee.getException().getMessage());
        }
        if (ee.getMojoExecution() != null) {
            return "failed: " + ee.getMojoExecution().getGoal();
        }
        return "build failed";
    }

    /** Maven colorizes its messages; strip the ANSI escapes so the dashboard shows clean text. */
    private static String stripAnsi(String s) {
        return s.replaceAll("\\u001B\\[[0-9;]*m", "");
    }

    /**
     * Ensures the portable {@code kysymys-agent} watcher is running for this
     * project. The Go binary self-deduplicates via its own lock file, so calling
     * this on every {@code mvn} run is safe — a second {@code watch} exits at
     * once. Detached: the child keeps running after this Maven process exits.
     *
     * <p>The binary is downloaded from the kysymys server on first build and
     * cached. If none is served for this host (or the download fails) the
     * watcher is skipped; build outcomes still report over HTTP, so the
     * dashboard keeps working.
     */
    private void ensureWatcher() {
        try {
            Path binary = AgentBinary.ensureDownloaded(config.url());
            if (binary == null) {
                return;
            }
            Path project = config.workingDir();
            Path logFile = project.resolve("target").resolve("kysymys-watcher.log");
            Files.createDirectories(logFile.getParent());

            ProcessBuilder pb = new ProcessBuilder(
                    binary.toString(), "watch", project.toString())
                    .redirectOutput(logFile.toFile())
                    .redirectError(logFile.toFile());
            pb.environment().put("KYSYMYS_URL", config.url());
            pb.environment().put("KYSYMYS_TOKEN", config.token());
            if (config.problemId() != null) {
                pb.environment().put("KYSYMYS_PROBLEM_ID", config.problemId());
            }
            pb.start(); // intentionally not waited on — detached heartbeat process
        } catch (Exception ignored) {
            // If the watcher can't start, build-event reporting still covers the
            // build-error and (coarsely) the inactivity signals.
        }
    }
}
