package jdplus.main.desktop.bin;

import java.util.logging.Level;
import junit.framework.Test;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;

/**
 * @author Christiane.Hofer@bundesbank.de
 */
public class ApplicationScenario extends NbTestCase {

    public static Test suite() {
        return NbModuleSuite.createConfiguration(ApplicationScenario.class)
                .gui(true)
                .failOnMessage(Level.SEVERE)
                .failOnException(Level.SEVERE)
                .clusters(".*")
                .enableModules("org.netbeans.modules.jellytools.platform")
                .suite();
    }

    public ApplicationScenario(String name) {
        super(name);
    }

    public void testApplication() {
        // pass if there are merely no warnings/exceptions
        new ActionNoBlock("Help|About", null).performMenu();
        new NbDialogOperator("About").closeByButton();
    }
}
