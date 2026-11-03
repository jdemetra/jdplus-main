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
                .failOnMessage(null) // FIXME: replace null with Level.SEVERE when java-desktop-util-os is fixed
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
        // Invoked through the API since the menu item is moved to the native application menu on macOS
        new ActionNoBlock(null, null, "org.netbeans.core.actions.AboutAction").performAPI();
        new NbDialogOperator("About").closeByButton();
    }
}
