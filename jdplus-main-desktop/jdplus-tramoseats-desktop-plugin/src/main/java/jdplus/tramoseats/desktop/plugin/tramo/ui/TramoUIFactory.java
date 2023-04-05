/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.tramoseats.desktop.plugin.tramo.ui;

import jdplus.toolkit.desktop.plugin.descriptors.IObjectDescriptor;
import jdplus.tramoseats.desktop.plugin.tramo.descriptors.TramoSpecUI;
import jdplus.toolkit.desktop.plugin.ui.processing.IProcDocumentView;
import jdplus.toolkit.desktop.plugin.workspace.DocumentUIServices;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.tramoseats.base.api.tramo.TramoSpec;
import java.awt.Color;
import javax.swing.Icon;
import jdplus.tramoseats.base.core.tramo.TramoDocument;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@ServiceProvider(service = DocumentUIServices.class)
public class TramoUIFactory implements DocumentUIServices<TramoSpec, TramoDocument> {
    
//    public static TramoUIFactory INSTANCE=new TramoUIFactory();

    @Override
    public IProcDocumentView<TramoDocument> getDocumentView(TramoDocument document) {
        return TramoViewFactory.getDefault().create(document);
    }

    @Override
    public IObjectDescriptor<TramoSpec> getSpecificationDescriptor(TramoSpec spec) {
        return new TramoSpecUI(spec, false);
    }

    @Override
    public Class<TramoDocument> getDocumentType() {
        return TramoDocument.class; 
    }

    @Override
    public Class<TramoSpec> getSpecType() {
        return TramoSpec.class; 
    }

    @Override
    public Color getColor() {
        return Color.BLUE; 
    }

    @Override
    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("jdplus/tramoseats/desktop/plugin/tramo/ui/tangent_blue.png", false);
    }

    @Override
    public void showDocument(WorkspaceItem<TramoDocument> item) {
        if (item.isOpen()) {
            item.getView().requestActive();
        } else {
            TramoTopComponent view = new TramoTopComponent(item);
            view.open();
            view.requestActive();
        }
    }

}
