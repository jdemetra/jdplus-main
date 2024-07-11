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
package jdplus.sa.desktop.plugin.util;

import jdplus.sa.desktop.plugin.l2fprod.SaInterventionVariableDescriptor;
import jdplus.sa.desktop.plugin.l2fprod.SaInterventionVariablesEditor;
import jdplus.sa.desktop.plugin.l2fprod.SaTsVariableDescriptor;
import jdplus.sa.desktop.plugin.l2fprod.SaTsVariableDescriptorsEditor;
import jdplus.sa.desktop.plugin.output.OutputFactoryBuddies;
import jdplus.sa.desktop.plugin.ui.DemetraSaUI;
import jdplus.toolkit.desktop.plugin.ui.properties.l2fprod.ArrayRenderer;
import jdplus.toolkit.desktop.plugin.ui.properties.l2fprod.CustomPropertyEditorRegistry;
import jdplus.toolkit.desktop.plugin.ui.properties.l2fprod.CustomPropertyRendererFactory;
import jdplus.toolkit.desktop.plugin.util.InstallerStep;
import org.openide.modules.ModuleInstall;

import java.util.prefs.Preferences;

public final class Installer extends ModuleInstall {

    public static final InstallerStep STEP = InstallerStep.all(
            new DemetraSaOptionsStep(), new SaOutputStep(), new PropertiesStep()
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

    private static final class DemetraSaOptionsStep extends InstallerStep {

        final Preferences prefs = prefs().node("options");

        @Override
        public void restore() {
            load(prefs, DemetraSaUI.get());
        }

        @Override
        public void close() {
            store(prefs, DemetraSaUI.get());
        }
    }

    private static final class SaOutputStep extends InstallerStep {

        private final Preferences outputs = prefs().node("outputs");

        @Override
        public void restore() {
            OutputFactoryBuddies.getInstance().getFactories()
                    .forEach(buddy -> load(outputs.node(buddy.getDisplayName()), buddy));
        }

        @Override
        public void close() {
            OutputFactoryBuddies.getInstance().getFactories()
                    .forEach(buddy -> store(outputs.node(buddy.getDisplayName()), buddy));
        }
    }


    private static final class PropertiesStep extends InstallerStep {

        @Override
        public void restore() {
            CustomPropertyEditorRegistry.INSTANCE.register(SaInterventionVariableDescriptor[].class, new SaInterventionVariablesEditor());
            CustomPropertyRendererFactory.INSTANCE.getRegistry().registerRenderer(SaInterventionVariableDescriptor[].class, new ArrayRenderer());
            CustomPropertyEditorRegistry.INSTANCE.register(SaTsVariableDescriptor[].class, new SaTsVariableDescriptorsEditor());
            CustomPropertyRendererFactory.INSTANCE.getRegistry().registerRenderer(SaTsVariableDescriptor[].class, new ArrayRenderer());
            CustomPropertyEditorRegistry.INSTANCE.register(SaInterventionVariableDescriptor[].class, new SaInterventionVariablesEditor());
        }

        @Override
        public void close() {
            CustomPropertyEditorRegistry.INSTANCE.unregister(SaInterventionVariableDescriptor[].class);
            CustomPropertyRendererFactory.INSTANCE.getRegistry().unregisterRenderer(SaInterventionVariableDescriptor[].class);
            CustomPropertyEditorRegistry.INSTANCE.unregister(SaTsVariableDescriptor[].class);
            CustomPropertyRendererFactory.INSTANCE.getRegistry().unregisterRenderer(SaTsVariableDescriptor[].class);
        }
    }
}
