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

import jdplus.toolkit.desktop.plugin.ui.processing.ItemUI;
import javax.swing.JComponent;
import jdplus.x13.base.core.x13.X13Document;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class X13Summary implements ItemUI<X13Document>{

    @Override
    public JComponent getView(X13Document document) {
        JX13Summary view=new JX13Summary();
        view.set(document);
        return view;
    }
    
}