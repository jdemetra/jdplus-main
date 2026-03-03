/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package internal.toolkit.desktop.plugin.actions;

import jdplus.toolkit.desktop.plugin.actions.AbilityNodeAction;
import nbbrd.design.ClassNameConstant;
import org.netbeans.api.actions.Closable;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

import java.util.stream.Stream;

@ActionID(category = "File", id = CloseNodeAction.ID)
@ActionRegistration(displayName = "#CloseNodeAction", lazy = false)
@Messages("CloseNodeAction=Close")
public final class CloseNodeAction extends AbilityNodeAction<Closable> {

    @ClassNameConstant
    public static final String ID = "internal.toolkit.desktop.plugin.actions.CloseNodeAction";

    public CloseNodeAction() {
        super(Closable.class);
    }

    @Override
    protected void performAction(Stream<Closable> items) {
        items.forEach(Closable::close);
    }

    @Override
    public String getName() {
        return Bundle.CloseNodeAction();
    }
}
