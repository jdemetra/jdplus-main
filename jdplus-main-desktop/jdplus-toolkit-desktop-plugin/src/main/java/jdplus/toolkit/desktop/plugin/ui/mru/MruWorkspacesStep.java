/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui.mru;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import jdplus.toolkit.desktop.plugin.util.InstallerStep;

/**
 *
 * @author Philippe Charles
 */
public class MruWorkspacesStep extends InstallerStep {

    final Preferences prefsWs = NbPreferences.forModule(MruWorkspacesStep.class).node("MruWs");

    @Override
    public void restore() {
        MruPreferences.INSTANCE.load(prefsWs, MruList.getWorkspacesInstance());
    }

    @Override
    public void close() {
        MruPreferences.INSTANCE.store(prefsWs, MruList.getWorkspacesInstance());
    }
}
