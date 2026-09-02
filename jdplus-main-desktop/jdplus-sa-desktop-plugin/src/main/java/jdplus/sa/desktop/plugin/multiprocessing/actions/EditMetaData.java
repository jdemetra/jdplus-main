package jdplus.sa.desktop.plugin.multiprocessing.actions;

import jdplus.sa.desktop.plugin.multiprocessing.ui.MetaDataConfigurator;
import jdplus.sa.desktop.plugin.multiprocessing.ui.MultiProcessingManager;
import jdplus.sa.desktop.plugin.multiprocessing.ui.SaBatchUI;
import jdplus.sa.desktop.plugin.multiprocessing.ui.SaNode;
import jdplus.toolkit.desktop.plugin.ui.ActiveViewAction;
import nbbrd.design.ClassNameConstant;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "SaProcessing", id = EditMetaData.ID)
@ActionRegistration(displayName = "#CTL_EditMetaData", lazy = false)
@ActionReferences({
    @ActionReference(path = MultiProcessingManager.CONTEXTPATH + Edit.PATH, position = 1820)
})
@Messages({"CTL_EditMetaData=Metadata..."})
public class EditMetaData extends ActiveViewAction<SaBatchUI> {

    @ClassNameConstant
    public static final String ID = "jdplus.sa.desktop.plugin.multiprocessing.actions.EditMetaData";

    public static final String TITLE = "Metadata";

    public EditMetaData() {
        super(SaBatchUI.class);
        refreshAction();
        putValue(NAME, Bundle.CTL_EditMetaData());
    }

    @Override
    final protected void refreshAction() {
        SaBatchUI ui = context();
        enabled = ui != null && ui.getSelectionCount() == 1;
    }

    @Override
    protected void process(SaBatchUI cur) {
        SaNode[] selection = cur.getSelection();
        if (selection != null && selection.length == 1) {
            MetaDataConfigurator configurator = new MetaDataConfigurator(cur, selection[0]);
            configurator.setVisible(true);
            cur.setSelection(new SaNode[0]);
            cur.setSelection(selection);
        }
    }
}
