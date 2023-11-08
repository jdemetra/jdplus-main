/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.workspace;

import com.l2fprod.common.propertysheet.PropertySheetPanel;
import jdplus.toolkit.base.api.design.ExtensionPoint;
import jdplus.toolkit.base.api.processing.ProcDocument;
import jdplus.toolkit.base.api.processing.ProcSpecification;
import jdplus.toolkit.desktop.plugin.descriptors.IObjectDescriptor;
import jdplus.toolkit.desktop.plugin.ui.processing.IProcDocumentView;
import jdplus.toolkit.desktop.plugin.ui.properties.l2fprod.PropertiesPanelFactory;
import nbbrd.service.Mutability;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import org.openide.util.Lookup;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

/**
 *
 * @author Jean Palate
 * @param <S>
 * @param <D>
 */
@ExtensionPoint
@ServiceDefinition(quantifier = Quantifier.MULTIPLE, mutability = Mutability.NONE, singleton = true)
public interface DocumentUIServices<S extends ProcSpecification, D extends ProcDocument<S, ?, ?>> {

    public final String SPEC_PROPERTY = "specification";

    Class<D> getDocumentType();

    Class<S> getSpecType();

    IObjectDescriptor<S> getSpecificationDescriptor(S specification);

    IProcDocumentView<D> getDocumentView(D document);
    
    default PropertySheetPanel getSpecView(IObjectDescriptor<S> desc) {
        final PropertySheetPanel panel = PropertiesPanelFactory.INSTANCE.createPanel(desc);
        panel.addPropertySheetChangeListener(evt -> panel.firePropertyChange(SPEC_PROPERTY, 0, 1));
        return panel;
    }

    Color getColor();

    Icon getIcon();
    
    default Icon getItemIcon(WorkspaceItem<D> doc){
        return getIcon();
    }

    void showDocument(WorkspaceItem<D> doc);

    public static DocumentUIServices forSpec(Class sclass) {
        Optional<? extends DocumentUIServices> s = Lookup.getDefault().lookupAll(DocumentUIServices.class).stream()
                .filter(ui->ui.getSpecType().equals(sclass)).findFirst();
      
        return s.orElse(null);
    }

    public static DocumentUIServices forDocument(Class dclass) {
        Optional<? extends DocumentUIServices> s = Lookup.getDefault().lookupAll(DocumentUIServices.class).stream()
                .filter(ui->ui.getDocumentType().equals(dclass)).findFirst();
      
        return s.orElse(null);
    }
}
