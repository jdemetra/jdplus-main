/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.x13.desktop.plugin.x13.ui;

import jdplus.toolkit.desktop.plugin.descriptors.IObjectDescriptor;
import jdplus.toolkit.desktop.plugin.ui.processing.IProcDocumentView;
import jdplus.toolkit.desktop.plugin.workspace.DocumentUIServices;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.x13.desktop.plugin.x13.descriptors.X13SpecUI;
import jdplus.x13.base.api.x13.X13Spec;
import java.awt.Color;
import javax.swing.Icon;
import jdplus.x13.base.core.x13.X13Document;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@ServiceProvider(service = DocumentUIServices.class)
public class X13UIFactory implements DocumentUIServices<X13Spec, X13Document> {

    //   public static X13UIFactory INSTANCE=new X13UIFactory();
    @Override
    public IProcDocumentView<X13Document> getDocumentView(X13Document document) {
        return X13ViewFactory.getDefault().create(document);
    }

    @Override
    public IObjectDescriptor<X13Spec> getSpecificationDescriptor(X13Spec spec) {
        return new X13SpecUI(spec, false);
    }

    @Override
    public Class<X13Document> getDocumentType() {
        return X13Document.class;
    }

    @Override
    public Class<X13Spec> getSpecType() {
        return X13Spec.class;
    }

    @Override
    public Color getColor() {
        return Color.MAGENTA;
    }

    @Override
    public void showDocument(WorkspaceItem<X13Document> item) {
        if (item.isOpen()) {
            item.getView().requestActive();
        } else {
            X13TopComponent view = new X13TopComponent(item);
            view.open();
            view.requestActive();
        }
    }

    @Override
    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("jdplus/x13/desktop/plugin/x13/ui/tangent_magenta.png", false);
    }

}
