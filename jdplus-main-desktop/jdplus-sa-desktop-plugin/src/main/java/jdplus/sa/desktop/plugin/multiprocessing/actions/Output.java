/*
 * Copyright 2015 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package jdplus.sa.desktop.plugin.multiprocessing.actions;

import jdplus.toolkit.desktop.plugin.util.SingleFileExporter;
import jdplus.toolkit.desktop.plugin.notification.MessageType;
import jdplus.toolkit.desktop.plugin.notification.NotifyUtil;
import jdplus.sa.desktop.plugin.multiprocessing.ui.MultiProcessingDocument;
import jdplus.sa.desktop.plugin.multiprocessing.ui.MultiProcessingManager;
import jdplus.sa.desktop.plugin.multiprocessing.ui.SaBatchUI;
import jdplus.sa.desktop.plugin.output.OutputPanel;
import jdplus.toolkit.desktop.plugin.ui.ActiveViewAction;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.sa.base.api.SaItem;
import jdplus.sa.base.api.SaOutputFactory;
import jdplus.sa.base.csv.CsvMatrixOutputFactory;
import jdplus.sa.base.csv.CsvOutputFactory;
import jdplus.sa.desktop.plugin.multiprocessing.ui.MultiProcessingController;
import jdplus.toolkit.base.api.util.LinearId;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import nbbrd.design.ClassNameConstant;
import org.jspecify.annotations.Nullable;
import javax.swing.SwingWorker;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "SaProcessing", id = Output.ID)
@ActionRegistration(displayName = "#CTL_Output", lazy = false)
@ActionReferences({
    @ActionReference(path = MultiProcessingManager.CONTEXTPATH, position = 2000, separatorBefore = 1999),
    @ActionReference(path = "Shortcuts", name = "O")
})
@Messages("CTL_Output=Output...")
public final class Output extends ActiveViewAction<SaBatchUI> {

    @ClassNameConstant
    public static final String ID = "jdplus.sa.desktop.plugin.multiprocessing.actions.Output";

    public Output() {
        super(SaBatchUI.class);
        putValue(NAME, Bundle.CTL_Output());
        refreshAction();
    }

    @Override
    protected void refreshAction() {
        SaBatchUI ui = this.context();
        enabled = ui != null && ui.getElement() != null && ui.getController().getSaProcessingState() == MultiProcessingController.SaProcessingState.DONE;
    }

    @Override
    protected void process(SaBatchUI ui) {
        SaItem[] processing = ui.getElement().all();
        Optional<SaItem> np = Arrays.stream(processing).filter(p->! p.isProcessed()).findAny();
        if (processing.length == 0 || np.isPresent()) {
            return;
        }
        final OutputPanel panel = new OutputPanel();

        final DialogDescriptor dd = new DialogDescriptor(panel, "Batch output");

        if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION) {
            WorkspaceItem<MultiProcessingDocument> doc = ui.getController().getDocument();
            List<SaOutputFactory> outputs = panel.getFactories();
            LinearId id = new LinearId(doc.getOwner().getName(), doc.getDisplayName());
            for (SaOutputFactory output : outputs) {
                File target = getExportFolder(output);
                if (target != null) {
                    SingleFileExporter
                            .builder()
                            .file(target)
                            .progressLabel("Saving to " + output.getName())
                            .onErrorNotify("Saving to " + output.getName() + " failed")
                            .onSuccessNotify("Saving to " + output.getName() + " done")
                            .build()
                            .execAsync((f, ph) -> store(output, id, processing, ph));
                } else {
                    save(output, id, processing);
                }
            }
        }
    }

    @Nullable
    private File getExportFolder(SaOutputFactory output) {
         if (output instanceof CsvMatrixOutputFactory fac) {
            return fac.getConfiguration().getFolder();
        }
        if (output instanceof CsvOutputFactory fac) {
            return fac.getConfiguration().getFolder();
        }
        return null;
    }

    private static void store(SaOutputFactory output, LinearId id, SaItem[] processing, ProgressHandle ph) throws Exception {
        ph.start();
        ph.progress("Initializing");
        jdplus.toolkit.base.api.processing.Output sadoc = output.create();
        ph.progress("Starting");
        sadoc.start(id);
        ph.progress("Processing");
        for (SaItem cur : processing) {
            sadoc.process(cur.asDocument());
        }
        ph.progress("Ending");
        sadoc.end(id);
    }

    private void save(SaOutputFactory output, LinearId id, SaItem[] processing) {
        new SwingWorker<Void, String>() {
            final ProgressHandle progressHandle = ProgressHandle.createHandle("Saving to " + output.getName());

            @Override
            protected Void doInBackground() throws Exception {
                store(output, id, processing, progressHandle);
                return null;
            }

            @Override
            protected void done() {
                progressHandle.finish();
                try {
                    get();
                    NotifyUtil.show(output.getName() + " successfully generated", "", MessageType.SUCCESS);
                } catch (InterruptedException | ExecutionException ex) {
                    NotifyUtil.error("Can't generate output (" + output.getName() + ")", ex.getMessage(), ex);
                }
            }
        }.execute();
    }
}
