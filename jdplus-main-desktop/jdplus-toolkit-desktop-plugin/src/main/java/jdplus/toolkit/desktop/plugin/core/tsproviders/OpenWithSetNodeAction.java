/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
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
package jdplus.toolkit.desktop.plugin.core.tsproviders;

import jdplus.toolkit.desktop.plugin.NamedService;
import jdplus.toolkit.desktop.plugin.TsActionManager;
import jdplus.toolkit.desktop.plugin.TsManager;
import jdplus.toolkit.base.api.timeseries.TsInformationType;
import jdplus.toolkit.base.tsp.DataSet;
import nbbrd.design.ClassNameConstant;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Philippe Charles
 */
@ActionID(category = "Edit", id = OpenWithSetNodeAction.ID)
@ActionRegistration(displayName = "#CTL_OpenWithSetAction", lazy = false)
@NbBundle.Messages("CTL_OpenWithSetAction=Open with")
public final class OpenWithSetNodeAction extends AbstractAction implements Presenter.Popup {

    @ClassNameConstant
    public static final String ID = "jdplus.toolkit.desktop.plugin.core.tsproviders.OpenWithSetNodeAction";

    @Override
    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public JMenuItem getPopupPresenter() {
        Node selectedNode = Utilities.actionsGlobalContext().lookup(Node.class);
        DataSet dataSet = selectedNode.getLookup().lookup(DataSet.class);
        JMenu result = new JMenu(Bundle.CTL_OpenWithSetAction());
        for (NamedService o : TsActionManager.get().getOpenActions()) {
            result.add(new OpenTsAction(o, dataSet)).setText(o.getDisplayName());
        }
        return result;
    }

    @lombok.AllArgsConstructor
    private static final class OpenTsAction extends AbstractAction {

        private final NamedService tsAction;
        private final DataSet dataSet;

        @Override
        public void actionPerformed(ActionEvent e) {
            TsManager.get()
                    .getTs(dataSet, TsInformationType.None)
                    .ifPresent(o -> TsActionManager.get().openWith(o, tsAction.getName()));
        }
    }
}
