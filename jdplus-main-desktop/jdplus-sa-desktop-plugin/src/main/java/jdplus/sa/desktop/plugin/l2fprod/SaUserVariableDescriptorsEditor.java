/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.desktop.plugin.l2fprod;

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import jdplus.toolkit.desktop.plugin.ui.properties.l2fprod.ArrayEditorDialog;

/**
 *
 * @author Demortier Jeremy
 */
public class SaUserVariableDescriptorsEditor extends AbstractPropertyEditor {

    private static final SaUserVariableDescriptor[] EMPTY = new SaUserVariableDescriptor[0];

    private SaUserVariableDescriptor[] descriptors;

    public SaUserVariableDescriptorsEditor() {
        editor = new JButton(new AbstractAction("...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                final ArrayEditorDialog<SaUserVariableDescriptor> dialog = new ArrayEditorDialog<>(
                        SwingUtilities.getWindowAncestor(editor),
                        null != descriptors ? descriptors : EMPTY,
                        SaUserVariableDescriptor::new,
                        SaUserVariableDescriptor::duplicate);
                dialog.setTitle("Variables");
                dialog.setVisible(true);
                if (dialog.isDirty()) {
                    setDescriptors(dialog.getElements());
                }
            }
        });
    }

    private void setDescriptors(List<SaUserVariableDescriptor> elements) {
        SaUserVariableDescriptor[] old = descriptors;
        // check that the descriptors are well-formed
        descriptors = elements.toArray(SaUserVariableDescriptor[]::new);
        firePropertyChange(old, descriptors);
    }

    @Override
    public Object getValue() {
        return descriptors;
    }

    @Override
    public void setValue(Object value) {
        if (null != value && value instanceof SaUserVariableDescriptor[] vars) {
            SaUserVariableDescriptor[] old = descriptors;
            descriptors = vars;
            firePropertyChange(old, descriptors);
        } else descriptors = EMPTY;
    }
}
