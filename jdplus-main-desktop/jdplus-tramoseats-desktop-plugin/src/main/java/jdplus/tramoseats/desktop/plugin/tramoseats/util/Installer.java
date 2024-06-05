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

import jdplus.toolkit.desktop.plugin.util.InstallerStep;
import jdplus.tramoseats.desktop.plugin.tramoseats.diagnostics.TramoSeatsDiagnosticsFactoryBuddies;
import jdplus.tramoseats.desktop.plugin.tramoseats.ui.TramoSeatsUI;
import org.openide.modules.ModuleInstall;

import java.util.prefs.Preferences;

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
            TramoSeatsDiagnosticsFactoryBuddies.getInstance().getFactories()
                    .forEach(buddy-> load(prefs.node(buddy.getDisplayName()), buddy));
            TramoSeatsUI.setDiagnostics();
        }

        @Override
        public void close() {
            TramoSeatsDiagnosticsFactoryBuddies.getInstance().getFactories()
                    .forEach(buddy -> store(prefs.node(buddy.getDisplayName()), buddy));
        }
    }
    
    private static final class TramoSeatsOptionsStep extends InstallerStep {

        private final Preferences options = prefs().node("options");

        @Override
        public void restore() {
            load(options, TramoSeatsUI.get());
        }

        @Override
        public void close() {
            store(options, TramoSeatsUI.get());
        }
    }
}
