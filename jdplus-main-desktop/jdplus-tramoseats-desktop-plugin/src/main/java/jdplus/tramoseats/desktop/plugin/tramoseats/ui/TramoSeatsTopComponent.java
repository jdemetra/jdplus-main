/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.desktop.plugin.tramoseats.ui;

import jdplus.toolkit.base.api.timeseries.Ts;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.tramoseats.desktop.plugin.tramoseats.documents.TramoSeatsDocumentManager;
import jdplus.toolkit.desktop.plugin.ui.processing.TsProcessingViewer;
import jdplus.toolkit.desktop.plugin.ui.properties.l2fprod.UserInterfaceContext;
import jdplus.toolkit.desktop.plugin.workspace.DocumentUIServices;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceFactory;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.ui.WorkspaceTsTopComponent;
import jdplus.tramoseats.base.api.tramoseats.TramoSeatsSpec;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsDocument;
import nbbrd.design.ClassNameConstant;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@TopComponent.Description(
        preferredID = "TramoSeatsTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Seasonal Adjustment", id = TramoSeatsTopComponent.ID)
@ActionReference(path = "Menu/Statistical methods/Seasonal Adjustment/Single Analysis", position = 1000)
@TopComponent.OpenActionRegistration(displayName = "#CTL_TramoSeatsAction")
@NbBundle.Messages({
    "CTL_TramoSeatsAction=TramoSeats",
    "CTL_TramoSeatsTopComponent=TramoSeats Window",
    "HINT_TramoSeatsTopComponent=This is a TramoSeats window"
})
public final class TramoSeatsTopComponent extends WorkspaceTsTopComponent<TramoSeatsDocument> {

    @ClassNameConstant
    public static final String ID = "jdplus.tramoseats.desktop.plugin.tramoseats.ui.TramoSeatsTopComponent";

    private final ExplorerManager mgr = new ExplorerManager();

    private static TramoSeatsDocumentManager manager() {
        return WorkspaceFactory.getInstance().getManager(TramoSeatsDocumentManager.class);
    }

    public TramoSeatsTopComponent() {
        this(null);
    }

    public TramoSeatsTopComponent(WorkspaceItem<TramoSeatsDocument> doc) {
        super(doc);
        initComponents();
        setToolTipText(NbBundle.getMessage(TramoSeatsTopComponent.class, "HINT_TramoSeatsTopComponent"));
        associateLookup(ExplorerUtils.createLookup(mgr, getActionMap()));
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return mgr;
    }

    @Override
    public WorkspaceItem<TramoSeatsDocument> newDocument() {
        return manager().create(WorkspaceFactory.getInstance().getActiveWorkspace());
    }

    @Override
    protected TsProcessingViewer initViewer() {
        return TsProcessingViewer.create(getElement(), DocumentUIServices.forDocument(TramoSeatsDocument.class));
    }

    @Override
    public void componentActivated() {
        super.componentActivated();
        updateUserInterfaceContext();
    }

    @Override
    public void componentDeactivated() {
        super.componentDeactivated();
        UserInterfaceContext.INSTANCE.setAnnualFrequency(0);
    }

    private void initComponents() {
        setLayout(new java.awt.BorderLayout());
    }

    private void updateUserInterfaceContext() {
        if (getDocument() == null) {
            return;
        }
        TramoSeatsDocument element = getElement();
        if (element == null) {
            UserInterfaceContext.INSTANCE.setDomain(null);
        } else {
            TramoSeatsSpec s = element.getSpecification();
            if (s == null) {
                UserInterfaceContext.INSTANCE.setAnnualFrequency(0);
            } else {
                UserInterfaceContext.INSTANCE.setAnnualFrequency(s.getTramo().getFrequency());
            }
        }
    }

    @Override
    protected String getContextPath() {
        return TramoSeatsDocumentManager.CONTEXTPATH;
    }

    @Override
    public boolean update(TramoSeatsDocument element, Ts s) {
        if (s != null) {
            TsDomain domain = s.getData().getDomain();
            UserInterfaceContext.INSTANCE.setDomain(domain);
            TramoSeatsSpec nspec = element.getSpecification().setFrequency(domain.getAnnualFrequency());
            element.set(nspec, s);
        } else {
            UserInterfaceContext.INSTANCE.setDomain(null);
            element.set(s);
        }
        return true;
    }

}
