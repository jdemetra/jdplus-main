/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.tramoseats.desktop.plugin.tramoseats.util;

import jdplus.toolkit.desktop.plugin.Config;
import jdplus.tramoseats.desktop.plugin.tramoseats.diagnostics.TramoSeatsDiagnosticsFactoryBuddies;
import jdplus.tramoseats.desktop.plugin.tramoseats.ui.TramoSeatsUI;
import jdplus.toolkit.desktop.plugin.util.InstallerStep;

import static jdplus.toolkit.desktop.plugin.util.InstallerStep.tryGet;
import java.util.prefs.BackingStoreException;

import org.openide.modules.ModuleInstall;

import java.util.prefs.Preferences;
import org.openide.util.Exceptions;

public final class Installer extends ModuleInstall {

    public static final InstallerStep STEP = InstallerStep.all(
            new DemetraTramoSeatsDiagnosticsStep(), new TramoSeatsOptionsStep()
    );

    @Override
    public void restored() {
        super.restored();
        STEP.restore();
    }

    @Override
    public void close() {
        STEP.close();
        super.close();
    }

    private static final class DemetraTramoSeatsDiagnosticsStep extends InstallerStep {

        final Preferences prefs = prefs().node("diagnostics");

        @Override
        public void restore() {
            TramoSeatsDiagnosticsFactoryBuddies.getInstance().getFactories().forEach(buddy->{
                    Preferences nprefs = prefs.node(buddy.getDisplayName());
                    tryGet(nprefs).ifPresent(buddy::setConfig);
            });
            TramoSeatsUI.setDiagnostics();
        }

        @Override
        public void close() {
            TramoSeatsDiagnosticsFactoryBuddies.getInstance().getFactories().forEach(buddy->{
                Config config = buddy.getConfig();
                if (config != null){
                    Preferences nprefs = prefs.node(buddy.getDisplayName());
                    put(nprefs, config);
                    try {
                        nprefs.flush();
                    } catch (BackingStoreException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        }
    }
    
    private static final class TramoSeatsOptionsStep extends InstallerStep {

        final Preferences prefs = prefs().node("options");

        @Override
        public void restore() {
            TramoSeatsUI ui = TramoSeatsUI.get();
            tryGet(prefs).ifPresent(ui::setConfig);
        }

        @Override
        public void close() {
            TramoSeatsUI ui = TramoSeatsUI.get();
            put(prefs, ui.getConfig());
            try {
                prefs.flush();
            } catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
