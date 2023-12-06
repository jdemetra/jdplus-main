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
package jdplus.x13.desktop.plugin.x13.util;

import jdplus.toolkit.desktop.plugin.Config;
import jdplus.toolkit.desktop.plugin.ui.properties.l2fprod.ArrayRenderer;
import jdplus.toolkit.desktop.plugin.ui.properties.l2fprod.CustomPropertyEditorRegistry;
import jdplus.toolkit.desktop.plugin.ui.properties.l2fprod.CustomPropertyRendererFactory;
import jdplus.toolkit.desktop.plugin.util.InstallerStep;
import jdplus.x13.desktop.plugin.x13.descriptors.SeasonalFilterPropertyEditor;
import jdplus.x13.desktop.plugin.x13.diagnostics.X13DiagnosticsFactoryBuddies;
import jdplus.x13.desktop.plugin.x13.ui.X13UI;
import jdplus.x13.base.api.x11.SeasonalFilterOption;
import java.util.prefs.BackingStoreException;

import org.openide.modules.ModuleInstall;

import java.util.prefs.Preferences;
import jdplus.x13.base.api.x11.SigmaVecOption;
import jdplus.x13.desktop.plugin.x13.descriptors.SigmaVecPropertyEditor;
import org.openide.util.Exceptions;

public final class Installer extends ModuleInstall {

    public static final InstallerStep STEP = InstallerStep.all(
            new DemetraX13DiagnosticsStep(), new PropertiesStep(), new X13OptionsStep()
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

    private static final class DemetraX13DiagnosticsStep extends InstallerStep {

        final Preferences prefs = prefs().node("diagnostics");

        @Override
        public void restore() {
            X13DiagnosticsFactoryBuddies.getInstance().getFactories().forEach(buddy -> {
                Preferences nprefs = prefs.node(buddy.getDisplayName());
                tryGet(nprefs).ifPresent(buddy::setConfig);
            });
            X13UI.setDiagnostics();
        }

        @Override
        public void close() {
            X13DiagnosticsFactoryBuddies.getInstance().getFactories().forEach(buddy -> {
                Config config = buddy.getConfig();
                Preferences nprefs = prefs.node(buddy.getDisplayName());
                put(nprefs, config);
                try {
                    nprefs.flush();
                } catch (BackingStoreException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
        }
    }

    private static final class PropertiesStep extends InstallerStep {

        @Override
        public void restore() {
            CustomPropertyEditorRegistry.INSTANCE.register(SeasonalFilterOption[].class, new SeasonalFilterPropertyEditor());
            CustomPropertyRendererFactory.INSTANCE.getRegistry().registerRenderer(SeasonalFilterOption[].class, new ArrayRenderer());
            CustomPropertyEditorRegistry.INSTANCE.register(SigmaVecOption[].class, new SigmaVecPropertyEditor());
            CustomPropertyRendererFactory.INSTANCE.getRegistry().registerRenderer(SigmaVecOption[].class, new ArrayRenderer());
        }

        @Override
        public void close() {
            CustomPropertyEditorRegistry.INSTANCE.unregister(SeasonalFilterOption[].class);
            CustomPropertyEditorRegistry.INSTANCE.unregister(SigmaVecOption[].class);
        }
    }

    private static final class X13OptionsStep extends InstallerStep {

        final Preferences prefs = prefs().node("options");

        @Override
        public void restore() {
            X13UI ui = X13UI.get();
            tryGet(prefs).ifPresent(ui::setConfig);
        }

        @Override
        public void close() {
            X13UI ui = X13UI.get();
            put(prefs, ui.getConfig());
            try {
                prefs.flush();
            } catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

}
