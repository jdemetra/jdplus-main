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
package jdplus.toolkit.desktop.plugin.ui.calendar.actions;

import jdplus.toolkit.desktop.plugin.Config;
import jdplus.toolkit.desktop.plugin.Converter;
import jdplus.toolkit.desktop.plugin.interchange.Importable;
import jdplus.toolkit.desktop.plugin.interchange.InterchangeManager;
import jdplus.toolkit.desktop.plugin.nodes.SingleNodeAction;
import jdplus.toolkit.desktop.plugin.workspace.CalendarDocumentManager;
import jdplus.toolkit.desktop.plugin.workspace.Workspace;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceFactory;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.nodes.ItemWsNode;
import jdplus.toolkit.base.api.timeseries.calendars.CalendarDefinition;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import nbbrd.design.ClassNameConstant;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;

import javax.swing.*;
import java.util.List;

/**
 *
 * @author Philippe Charles
 */
@ActionID(category = "Edit", id = ImportCalendarAction.ID)
@ActionRegistration(displayName = "#CTL_ImportCalendarAction", lazy = false)
@ActionReferences({
    @ActionReference(path = CalendarDocumentManager.PATH, position = 1430)
})
@Messages("CTL_ImportCalendarAction=Import from")
public final class ImportCalendarAction extends SingleNodeAction<ItemWsNode> implements Presenter.Popup {

    @ClassNameConstant
    public static final String ID = "jdplus.toolkit.desktop.plugin.ui.calendar.actions.ImportCalendarAction";

    private static final Converter<Config, CalendarDefinition> CONVERTER = new CalendarConfig().reverse();
    private static final List<Importable> IMPORTABLES = List.of(new ImportableCalendar());

    public ImportCalendarAction() {
        super(ItemWsNode.class);
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenuItem result = InterchangeManager.get().newImportMenu(IMPORTABLES);
        result.setText(Bundle.CTL_ImportCalendarAction());
        return result;
    }

    @Override
    protected void performAction(ItemWsNode activatedNode) {
    }

    @Override
    protected boolean enable(ItemWsNode activatedNode) {
        return true;
    }

    @Override
    public String getName() {
        return null;
    }

    private static final class ImportableCalendar implements Importable {

        @Override
        public String getDomain() {
            return CalendarConfig.DOMAIN;
        }

        @Override
        public void importConfig(Config config) throws IllegalArgumentException {
            CalendarDefinition cal = CONVERTER.doForward(config);
            Workspace ws = WorkspaceFactory.getInstance().getActiveWorkspace();
            if (ws.searchDocumentByElement(cal) == null) {
                String name = ModellingContext.getActiveContext().getCalendars().get(cal);
                ws.add(WorkspaceItem.loadedItem(CalendarDocumentManager.ID, name, cal));
            }
        }
    }
}
