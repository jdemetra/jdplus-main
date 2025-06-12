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
package jdplus.sa.desktop.plugin.multiprocessing.ui;

import java.util.Collection;
import jdplus.toolkit.desktop.plugin.ui.ActiveView;
import jdplus.toolkit.desktop.plugin.ui.ActiveViewManager;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.sa.base.api.SaItem;
import jdplus.sa.base.api.SaItems;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceFactory;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jean Palate
 */
public abstract class AbstractSaProcessingTopComponent extends TopComponent implements ActiveView, MultiViewElement, MultiViewDescription, LookupListener {

    protected MultiProcessingController controller;
    private final Lookup.Result<WorkspaceFactory.Event> wsevent;

    public AbstractSaProcessingTopComponent() {
        this(new MultiProcessingController(null));
    }

    AbstractSaProcessingTopComponent(MultiProcessingController controller) {
        WorkspaceItem<MultiProcessingDocument> document = controller.getDocument();
        String txt = document.getDisplayName();
        setName(txt);
        setToolTipText(txt + " view");
        this.controller = controller;
        this.controller.addPropertyChangeListener(MultiProcessingController.SA_PROCESSING_STATE_PROPERTY, evt -> {
            firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            onSaProcessingStateChange();
        });
        this.wsevent = WorkspaceFactory.getInstance().getLookup().lookupResult(WorkspaceFactory.Event.class);
    }

    public MultiProcessingController getController() {
        return controller;
    }

    public SaItem[] current() {
        return controller.getDocument().getElement().all();
    }

    public SaItems getInitialProcessing() {
        return controller.getDocument().getElement().getInitial();
    }

    public MultiProcessingController.SaProcessingState getState() {
        return controller != null ? controller.getSaProcessingState() : MultiProcessingController.SaProcessingState.DONE;
    }

    protected void onSaProcessingStateChange() {
        this.getToolbarRepresentation().updateUI();
        this.getVisualRepresentation().updateUI();
    }

    protected void onSaProcessingSaved() {
    }

    //<editor-fold defaultstate="collapsed" desc="MultiViewElement">
    @Override
    public void componentOpened() {
        super.componentOpened();
        wsevent.addLookupListener(this);
    }

    @Override
    public void componentClosed() {
        super.componentClosed();
        wsevent.removeLookupListener(this);
        controller = null;
    }

    @Override
    public JComponent getVisualRepresentation() {
        return this;
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    @Override
    public void componentActivated() {
        super.componentActivated();
        ActiveViewManager.getInstance().set(this);
    }

    @Override
    public void componentDeactivated() {
        ActiveViewManager.getInstance().set(null);
        super.componentDeactivated();
    }

    @Override
    public void componentHidden() {
        super.componentHidden();
    }

    @Override
    public void componentShowing() {
        super.componentShowing();
    }

    //</editor-fold>
    @Override
    public void resultChanged(LookupEvent le) {
        Collection<? extends WorkspaceFactory.Event> all = wsevent.allInstances();
        for (WorkspaceFactory.Event ev : all) {
            switch (ev.info) {
                case WorkspaceFactory.Event.SAVE, WorkspaceFactory.Event.SAVEAS -> //if (ev.source != this) {
                    SwingUtilities.invokeLater(this::onSaProcessingSaved);
                //}
                }
        }
    }

    //<editor-fold defaultstate="collapsed" desc="MultiViewDescription">
    @Override
    public MultiViewElement createElement() {
        return this;
    }

    @Override
    public String preferredID() {
        return super.preferredID();
    }
    //</editor-fold>    

}
