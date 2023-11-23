/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.desktop.plugin.multiprocessing.actions;

import jdplus.sa.desktop.plugin.multiprocessing.ui.MultiProcessingManager;
import jdplus.toolkit.desktop.plugin.ui.Menus;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import nbbrd.design.ClassNameConstant;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;

@ActionID(category = "SaProcessing", id = RefSpecification.ID)
@ActionRegistration(displayName = "#CTL_RefSpecification", lazy = false)
@ActionReferences({
    @ActionReference(path = MultiProcessingManager.CONTEXTPATH + Edit.PATH, position = 1500, separatorBefore = 1499),
    @ActionReference(path = MultiProcessingManager.LOCALPATH + Edit.PATH, position = 1500, separatorBefore = 1499),
    @ActionReference(path = "Shortcuts", name = "r")
})
@Messages("CTL_RefSpecification=Reference specification")
public final class RefSpecification extends AbstractAction implements Presenter.Popup {

    @ClassNameConstant
    public static final String ID = "jdplus.sa.desktop.plugin.multiprocessing.actions.RefSpecification";

    public static final String PATH = Edit.PATH+"/RefSpecification";

    public RefSpecification() {
        super(Bundle.CTL_RefSpecification());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu menu = new JMenu(this);
        Menus.fillMenu(menu, MultiProcessingManager.CONTEXTPATH + PATH);
        return menu;
    }
}
