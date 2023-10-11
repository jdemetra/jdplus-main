/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.core.tsproviders;

import jdplus.toolkit.desktop.plugin.actions.AbilityNodeAction;
import jdplus.toolkit.desktop.plugin.datatransfer.DataSourceTransferManager;
import jdplus.toolkit.desktop.plugin.datatransfer.DataTransfers;
import jdplus.toolkit.base.tsp.DataSourceLoader;
import nbbrd.design.ClassNameConstant;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

import java.util.stream.Stream;

@ActionID(category = "Edit", id = PasteProviderNodeAction.ID)
@ActionRegistration(displayName = "#CTL_PasteProviderAction", lazy = false)
@Messages("CTL_PasteProviderAction=Paste")
public final class PasteProviderNodeAction extends AbilityNodeAction<DataSourceLoader> {

    @ClassNameConstant
    public static final String ID = "jdplus.toolkit.desktop.plugin.core.tsproviders.PasteProviderNodeAction";

    public PasteProviderNodeAction() {
        super(DataSourceLoader.class, true);
    }

    @Override
    protected void performAction(Stream<DataSourceLoader> items) {
        items.forEach(item -> {
            DataSourceTransferManager.get()
                    .getDataSource(DataTransfers.systemClipboardAsTransferable(), item.getSource())
                    .ifPresent(item::open);
        });
    }

    @Override
    protected boolean enable(Stream<DataSourceLoader> items) {
        return items.anyMatch(item -> DataSourceTransferManager.get().canHandle(DataTransfers.systemClipboardAsTransferable(), item.getSource()));
    }

    @Override
    public String getName() {
        return Bundle.CTL_PasteProviderAction();
    }
}
