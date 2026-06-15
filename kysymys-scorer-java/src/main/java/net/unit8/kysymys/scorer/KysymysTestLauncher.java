package net.unit8.kysymys.scorer;

import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

/**
 * Runs the JUnit Platform test plan for a single answer and prints the score.
 *
 * <p>The legacy HTTP scoring call ({@code POST /score/{submissionId}/{token}}) was removed when
 * kysymys-app was replatformed onto Enkan: the new server exposes no scoring endpoint. Scoring now
 * runs standalone and reports its result on stdout. A server-side scoring API is deferred to a
 * later step (after the full Bouncr stack is wired up).
 */
public class KysymysTestLauncher {
    public static void run(Class<?> testClass) {
        Launcher launcher = LauncherFactory.create();
        TestPlan plan = launcher.discover(LauncherDiscoveryRequestBuilder
                .request()
                .selectors(DiscoverySelectors.selectClass(testClass))
                .build());
        KysymysTestExecutionListener testExecutionListener = new KysymysTestExecutionListener();
        launcher.execute(plan, testExecutionListener);
        System.out.println(testExecutionListener);
    }
}
