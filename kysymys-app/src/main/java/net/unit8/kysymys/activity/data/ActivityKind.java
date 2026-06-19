package net.unit8.kysymys.activity.data;

/**
 * The kinds of work-activity telemetry a participant's machine reports.
 *
 * <ul>
 *   <li>{@code BUILD_SUCCESS} / {@code BUILD_FAILURE} — emitted by the Maven
 *       extension at the end of every {@code mvn} invocation.</li>
 *   <li>{@code HEARTBEAT} — emitted by the background watcher when files change,
 *       so inactivity can be measured even between builds.</li>
 *   <li>{@code STUCK} / {@code RESOLVED} — the participant's explicit
 *       "I need help" / "never mind" signals ({@code mvn kysymys:stuck} /
 *       {@code mvn kysymys:resolved}).</li>
 * </ul>
 */
public enum ActivityKind {
    BUILD_SUCCESS,
    BUILD_FAILURE,
    HEARTBEAT,
    STUCK,
    RESOLVED
}
