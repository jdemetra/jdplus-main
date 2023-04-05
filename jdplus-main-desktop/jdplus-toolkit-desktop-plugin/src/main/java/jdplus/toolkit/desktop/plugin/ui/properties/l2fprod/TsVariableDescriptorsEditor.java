/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui.properties.l2fprod;

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

/**
 *
 * @author Demortier Jeremy
 */
public class TsVariableDescriptorsEditor extends AbstractPropertyEditor {

    private static final TsVariableDescriptor[] EMPTY= new TsVariableDescriptor[0];

    private TsVariableDescriptor[] descriptors;

    public TsVariableDescriptorsEditor() {
        editor = new JButton(new AbstractAction("...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                final ArrayEditorDialog<TsVariableDescriptor> dialog = new ArrayEditorDialog<>(SwingUtilities.getWindowAncestor(editor),
                        null != descriptors ? descriptors : EMPTY, 
                        TsVariableDescriptor::new, 
                        TsVariableDescriptor::duplicate);
                dialog.setTitle("Variables");
                dialog.setVisible(true);
                if (dialog.isDirty()) {
                    setDescriptors(dialog.getElements());
                }
            }
        });
    }


    private void setDescriptors(List<TsVariableDescriptor> elements) {
        TsVariableDescriptor[] old = descriptors;
        // check that the descriptors are well-formed
       descriptors = elements.toArray(TsVariableDescriptor[]::new);
        firePropertyChange(old, descriptors);
    }

    @Override
    public Object getValue() {
        return descriptors;
    }

    @Override
    public void setValue(Object value) {
        if (null != value && value instanceof TsVariableDescriptor[] vars) {
            TsVariableDescriptor[] old = descriptors;
            descriptors = vars;
            firePropertyChange(old, descriptors);
        }else
            descriptors=EMPTY;
    }
}
