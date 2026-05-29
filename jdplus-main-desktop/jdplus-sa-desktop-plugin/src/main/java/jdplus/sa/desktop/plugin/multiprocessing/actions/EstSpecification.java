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

@ActionID(category = "SaProcessing", id = EstSpecification.ID)
@ActionRegistration(displayName = "#CTL_EstSpecification", lazy = false)
@ActionReferences({
    @ActionReference(path = MultiProcessingManager.CONTEXTPATH + Edit.PATH, position = 1510),
    @ActionReference(path = MultiProcessingManager.LOCALPATH + Edit.PATH, position = 1510)
})
@Messages("CTL_EstSpecification=Estimation specification")
public final class EstSpecification extends AbstractAction implements Presenter.Popup {

    @ClassNameConstant
    public static final String ID = "jdplus.sa.desktop.plugin.multiprocessing.actions.EstSpecification";

    public static final String PATH = Edit.PATH+"/EstSpecification";

    public EstSpecification() {
        super(Bundle.CTL_EstSpecification());
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
