/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.core.actions;

import jdplus.toolkit.desktop.plugin.actions.AbilityNodeAction;
import jdplus.toolkit.desktop.plugin.actions.Renameable;
import nbbrd.design.ClassNameConstant;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

import java.util.stream.Stream;

/**
 *
 * @author Philippe Charles
 */
@ActionID(category = "File", id = RenameNodeAction.ID)
@ActionRegistration(displayName = "#RenameNodeAction", lazy = false)
@NbBundle.Messages({"RenameNodeAction=Rename..."})
public final class RenameNodeAction extends AbilityNodeAction<Renameable> {

    @ClassNameConstant
    public static final String ID = "jdplus.toolkit.desktop.plugin.core.actions.RenameNodeAction";

    public RenameNodeAction() {
        super(Renameable.class);
    }

    @Override
    protected void performAction(Stream<Renameable> items) {
        items.forEach(Renameable::rename);
    }

    @Override
    public String getName() {
        return Bundle.RenameNodeAction();
    }
}
