/*
 * Copyright 2026 JDemetra+.
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
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
