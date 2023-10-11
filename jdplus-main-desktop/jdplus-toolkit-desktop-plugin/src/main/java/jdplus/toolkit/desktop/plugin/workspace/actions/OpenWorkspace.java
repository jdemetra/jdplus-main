package jdplus.toolkit.desktop.plugin.workspace.actions;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceFactory;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import nbbrd.design.ClassNameConstant;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "File", id = OpenWorkspace.ID)
@ActionRegistration(displayName = "#CTL_OpenWorkspace")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 200)
})
@Messages("CTL_OpenWorkspace=Open Workspace")
public final class OpenWorkspace implements ActionListener {

    @ClassNameConstant
    public static final String ID = "jdplus.toolkit.desktop.plugin.workspace.actions.OpenWorkspace";

    @Override
    public void actionPerformed(ActionEvent e) {
        WorkspaceFactory.getInstance().openWorkspace();
    }
}
