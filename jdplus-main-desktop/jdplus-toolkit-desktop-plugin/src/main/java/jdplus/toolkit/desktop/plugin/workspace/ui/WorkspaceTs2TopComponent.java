/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.workspace.ui;

import jdplus.toolkit.desktop.plugin.TsDynamicProvider;
import jdplus.toolkit.desktop.plugin.TsManager;
import jdplus.toolkit.desktop.plugin.ui.Menus;
import jdplus.toolkit.desktop.plugin.ui.processing.DefaultProcessingViewer;
import jdplus.toolkit.desktop.plugin.ui.processing.Ts2ProcessingViewer;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceFactory;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.base.api.timeseries.MultiTsDocument;
import jdplus.toolkit.base.api.timeseries.Ts;
import jdplus.toolkit.base.api.timeseries.TsInformationType;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JMenu;

/**
 *
 * @author Jean Palate
 * @param <T>
 */
public abstract class WorkspaceTs2TopComponent<T extends MultiTsDocument<?, ?>> extends WorkspaceTopComponent<T> {

    private Ts2ProcessingViewer<?, ?> panel;

    protected WorkspaceTs2TopComponent(WorkspaceItem<T> doc) {
        super(doc);
    }

//    public void updateUserInterfaceContext() {
//        if (doc == null) {
//            return;
//        }
//        T element = doc.getElement();
//        if (element == null) {
//            UserInterfaceContext.INSTANCE.setDomain(null);
//        } else {
//            Ts s = element.getInput();
//            if (s == null) {
//                UserInterfaceContext.INSTANCE.setDomain(null);
//            } else {
//                UserInterfaceContext.INSTANCE.setDomain(s.getData().getDomain());
//            }
//        }
//    }
//
//    @Override
//    public void componentActivated() {
//        super.componentActivated();
//        updateUserInterfaceContext();
//    }
    @Override
    public Action[] getActions() {
        return Menus.createActions(super.getActions(), WorkspaceFactory.TSCONTEXTPATH, getContextPath());
    }

    @Override
    public void refresh() {
        panel.onDocumentChanged();
    }

    protected abstract Ts2ProcessingViewer initViewer();

    @Override
    public void componentOpened() {
        super.componentOpened();
        panel = initViewer();
        WorkspaceItem<T> d = getDocument();
        add(panel);
        panel.refreshHeader();
        panel.addPropertyChangeListener((PropertyChangeEvent arg0) -> {
            switch (arg0.getPropertyName()) {
                case DefaultProcessingViewer.INPUT_CHANGED -> {
                    Object nval = arg0.getNewValue();
                    if (nval instanceof List) {
                        setTs((List<Ts>) nval);
                    }
                }
                case DefaultProcessingViewer.SPEC_CHANGED -> {
                    WorkspaceFactory.Event ev = new WorkspaceFactory.Event(d.getOwner(), d.getId(), WorkspaceFactory.Event.ITEMCHANGED, WorkspaceTs2TopComponent.this);
                    WorkspaceFactory.getInstance().notifyEvent(ev);
                }

            }
        });

        TsDynamicProvider.onDocumentOpened(panel.getDocument());
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        if (panel != null) {
            panel.removeListeners();
            panel.dispose();
        }
        TsDynamicProvider.onDocumentClosing(panel.getDocument());
        super.componentClosed();
    }

    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public boolean fill(JMenu menu) {
        Menus.fillMenu(menu, WorkspaceFactory.TSCONTEXTPATH, getContextPath());
        return true;
    }

    public void setTs(List<Ts> lts) {

        List<Ts> clts = new ArrayList<>();

        for (Ts ts : lts) {
            Ts cts;
            if (TsManager.isDynamic(ts)) {
                cts = ts.freeze();
            } else {
                cts = ts.load(TsInformationType.All, TsManager.get());
            }
            clts.add(cts);
        }
        panel.getDocument().set(clts);
        panel.updateButtons(null);
        WorkspaceItem<T> d = getDocument();
        d.setDirty();
        WorkspaceFactory.Event ev = new WorkspaceFactory.Event(d.getOwner(), d.getId(), WorkspaceFactory.Event.ITEMCHANGED, this);
        WorkspaceFactory.getInstance().notifyEvent(ev);

    }
}
