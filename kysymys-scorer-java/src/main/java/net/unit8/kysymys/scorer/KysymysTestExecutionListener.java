package net.unit8.kysymys.scorer;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

import java.util.Objects;

public class KysymysTestExecutionListener implements TestExecutionListener {
    private int successful = 0;
    private int failed = 0;
    private int aborted = 0;

    @Override
    public void executionFinished(TestIdentifier id, TestExecutionResult result) {
        if (!id.isTest()) return;
        switch(result.getStatus()) {
            case SUCCESSFUL: { successful++; break; }
            case FAILED: { failed++; break; }
            case ABORTED: { aborted++; break; }
        }
    }

    @Override
    public String toString() {
        return Objects.toString(successful * 100 / (successful + failed + aborted));
    }
}
