package jdplus.main.desktop.bin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.GraphicsEnvironment;
import junit.framework.TestResult;
import junit.textui.TestRunner;
import org.junit.jupiter.api.Test;

/**
 * Runs the NbModuleSuite-based UI scenarios.
 * <p>
 * NbModuleSuite creates its tests at runtime, which the JUnit Vintage engine cannot report,
 * so the suite is executed explicitly here.
 */
class ApplicationTest {

    @Test
    void testApplication() {
        assumeFalse(GraphicsEnvironment.isHeadless(), "UI tests require a display");
        TestResult result = TestRunner.run(ApplicationScenario.suite());
        assertThat(result.wasSuccessful()).isTrue();
    }
}
