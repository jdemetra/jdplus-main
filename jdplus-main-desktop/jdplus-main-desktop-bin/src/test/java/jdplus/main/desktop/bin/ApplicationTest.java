package jdplus.main.desktop.bin;

/**
 *
 * @author Christiane.Hofer@bundesbank.de
 */
import java.util.logging.Level;
import junit.framework.Test;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;

public class ApplicationTest extends NbTestCase {

    public static Test suite() {

        return NbModuleSuite.createConfiguration(ApplicationTest.class)
                .gui(true)
                .failOnMessage(Level.SEVERE)
                .failOnException(Level.SEVERE)
                .enableClasspathModules(false)
                .clusters(".*")
                .enableModules("org.netbeans.modules.jellytools.platform")
                .suite();
    }

    public ApplicationTest(String n) {
        // n="nbdemetra";
        super(n);
    }

    public void testApplication() {
        // pass if there are merely no warnings/exceptions
        // Example of using Jelly Tools (additional test dependencies required) with gui(true):
        new ActionNoBlock("Help|About", null).performMenu();
        new NbDialogOperator("About").closeByButton();
    }
}
