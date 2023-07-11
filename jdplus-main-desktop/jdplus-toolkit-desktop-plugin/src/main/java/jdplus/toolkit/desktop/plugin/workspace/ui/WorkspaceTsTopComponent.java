/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.workspace.ui;

import jdplus.toolkit.desktop.plugin.TsDynamicProvider;
import jdplus.toolkit.desktop.plugin.TsManager;
import jdplus.toolkit.desktop.plugin.components.parts.HasTs;
import jdplus.toolkit.desktop.plugin.ui.Menus;
import jdplus.toolkit.desktop.plugin.ui.processing.DefaultProcessingViewer;
import jdplus.toolkit.desktop.plugin.ui.processing.TsProcessingViewer;
import jdplus.toolkit.desktop.plugin.ui.properties.l2fprod.UserInterfaceContext;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceFactory;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.base.api.timeseries.Ts;
import jdplus.toolkit.base.api.timeseries.TsDocument;
import jdplus.toolkit.base.api.timeseries.TsInformationType;

import java.beans.PropertyChangeEvent;
import javax.swing.Action;
import javax.swing.JMenu;

/**
 *
 * @author Jean Palate
 * @param <T>
 */
public abstract class WorkspaceTsTopComponent<T extends TsDocument<?, ?>> extends WorkspaceTopComponent<T> implements HasTs {

    private TsProcessingViewer<?, ?> panel;

    protected WorkspaceTsTopComponent(WorkspaceItem<T> doc) {
        super(doc);
    }

    protected abstract TsProcessingViewer initViewer();

    public void updateUserInterfaceContext() {
        if (getDocument() == null) {
            return;
        }
        T element = getElement();
        if (element == null) {
            UserInterfaceContext.INSTANCE.setDomain(null);
        } else {
            Ts s = element.getInput();
            if (s == null) {
                UserInterfaceContext.INSTANCE.setDomain(null);
            } else {
                UserInterfaceContext.INSTANCE.setDomain(s.getData().getDomain());
            }
        }
    }

    @Override
    public void componentActivated() {
        super.componentActivated();
        updateUserInterfaceContext();
    }

    @Override
    public Action[] getActions() {
        return Menus.createActions(super.getActions(), WorkspaceFactory.TSCONTEXTPATH, getContextPath());
    }

    @Override
    public void refresh() {
        panel.onDocumentChanged();
    }

    @Override
    public void componentOpened() {
        super.componentOpened();
        WorkspaceItem<T> d = getDocument();
        TsDynamicProvider.onDocumentOpened(d.getElement());
        panel = initViewer();
        add(panel);
        panel.refreshHeader();
        panel.addPropertyChangeListener((PropertyChangeEvent arg0) -> {
            switch (arg0.getPropertyName()) {
                case DefaultProcessingViewer.INPUT_CHANGED -> {
                    Object nval = arg0.getNewValue();
                    if (nval instanceof Ts ts) {
                        setTs(ts);
                    }
                }
                case DefaultProcessingViewer.SPEC_CHANGED -> {
                    WorkspaceFactory.Event ev = new WorkspaceFactory.Event(d.getOwner(), d.getId(), WorkspaceFactory.Event.ITEMCHANGED, WorkspaceTsTopComponent.this);
                    WorkspaceFactory.getInstance().notifyEvent(ev);
                    d.setDirty();
                }

            }
        });

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

    @Override
    public Ts getTs() {
        return panel.getDocument().getInput();
    }

    @Override
    public void setTs(Ts ts) {
        Ts cts;
        if (TsManager.isDynamic(ts)) {
            cts = ts.freeze();
        } else {
            cts = ts.load(TsInformationType.All, TsManager.get()).freeze();
        }
        panel.getDocument().set(cts);
        panel.updateButtons(null);
        getDocument().setDirty();
        WorkspaceItem<T> d = getDocument();
        WorkspaceFactory.Event ev = new WorkspaceFactory.Event(d.getOwner(), d.getId(), WorkspaceFactory.Event.ITEMCHANGED, this);
        WorkspaceFactory.getInstance().notifyEvent(ev);

    }
}
