/*
 * Copyright 2026 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the Licence;
 * you may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package internal.toolkit.desktop.plugin.search;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import nbbrd.design.ClassNameConstant;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(category = "Tools", id = SearchEverywhereAction.ID)
@ActionRegistration(displayName = "#CTL_SearchEverywhereAction")
@ActionReferences({
    @ActionReference(path = "Menu/Tools", position = 10),
    @ActionReference(path = "Shortcuts", name = "DS-A")
})
@NbBundle.Messages("CTL_SearchEverywhereAction=Search Everywhere")
public final class SearchEverywhereAction implements ActionListener {

    @ClassNameConstant
    public static final String ID = "internal.toolkit.desktop.plugin.search.SearchEverywhereAction";

    @Override
    public void actionPerformed(ActionEvent e) {
        SearchEverywhereDialog.open();
    }
}
