/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui.calendar.actions;

import jdplus.toolkit.desktop.plugin.ui.calendar.ChainedGregorianCalendarPanel;
import jdplus.toolkit.desktop.plugin.ui.calendar.CompositeGregorianCalendarPanel;
import jdplus.toolkit.desktop.plugin.ui.calendar.NationalCalendarPanel;
import jdplus.toolkit.desktop.plugin.workspace.CalendarDocumentManager;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceFactory;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.nodes.ItemWsNode;
import jdplus.toolkit.base.api.timeseries.calendars.*;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.toolkit.base.api.util.WeightedItem;
import nbbrd.design.ClassNameConstant;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@ActionID(category = "Tools", id = AddCalendarAction.ID)
@ActionRegistration(displayName = "#CTL_AddCalendarAction", lazy = false)
@ActionReferences({
    @ActionReference(path = CalendarDocumentManager.PATH, position = 1000)
})
@Messages("CTL_AddCalendarAction=Add Calendar")
public final class AddCalendarAction extends AbstractAction implements Presenter.Popup, Presenter.Menu {

    @ClassNameConstant
    public static final String ID = "jdplus.toolkit.desktop.plugin.ui.calendar.actions.AddCalendarAction";

    @Override
    public void actionPerformed(ActionEvent ae) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Messages({
        "addCalendarAction.national=National",
        "addCalendarAction.chained=Chained",
        "addCalendarAction.composite=Composite"
    })
    
    @Override
    public JMenuItem getMenuPresenter() {
        JMenu result = new JMenu(Bundle.CTL_AddCalendarAction());
        result.add(new AbstractAction(Bundle.addCalendarAction_national()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNationalCalendar(ModellingContext.getActiveContext().getCalendars());
            }
        });
        result.add(new AbstractAction(Bundle.addCalendarAction_chained()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                addChainedCalendar(ModellingContext.getActiveContext().getCalendars());
            }
        });
        result.add(new AbstractAction(Bundle.addCalendarAction_composite()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                addCompositeCalendar(ModellingContext.getActiveContext().getCalendars());
            }
        });
        return result;
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return getMenuPresenter();
    }

    static CalendarDefinition getProvider(ItemWsNode activatedNode) {
        WorkspaceItem<CalendarDefinition> tmp = activatedNode.getItem(CalendarDefinition.class);
        return tmp != null ? tmp.getElement() : null;
    }

    static void add(CalendarManager manager, String name, CalendarDefinition p) {
        manager.set(name, p);
        WorkspaceFactory.getInstance().getActiveWorkspace().add(CalendarDocumentManager.newItem(name, p));
    }

    @Messages({
        "addNationalCalendar.dialog.title=Add National Calendar"
    })
    static void addNationalCalendar(CalendarManager manager) {
        NationalCalendarPanel panel = new NationalCalendarPanel();

        DialogDescriptor dd = panel.createDialogDescriptor(Bundle.addNationalCalendar_dialog_title());
        if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION) {
            Collection<Holiday> events = panel.getHolidays();
            Calendar np = new Calendar(events.toArray(new Holiday[events.size()]));
            add(manager, panel.getCalendarName(), np);
        }
    }

    @Messages({
        "addChainedCalendar.dialog.title=Add Chained Gregorian Calendar"
    })
    static void addChainedCalendar(CalendarManager manager) {
        ChainedGregorianCalendarPanel panel = new ChainedGregorianCalendarPanel();

        DialogDescriptor dd = panel.createDialogDescriptor(Bundle.addChainedCalendar_dialog_title());
        if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION) {
            String first = panel.getFirstCalendar();
            String second = panel.getSecondCalendar();
            LocalDate dayBreak = panel.getDayBreak();
            add(manager, panel.getCalendarName(), new ChainedCalendar(first, second, dayBreak));
        }
    }

    @Messages({
        "addCompositeCalendar.dialog.title=Add Composite Gregorian Calendar"
    })
    static void addCompositeCalendar(CalendarManager manager) {
        CompositeGregorianCalendarPanel panel = new CompositeGregorianCalendarPanel("");

        DialogDescriptor dd = panel.createDialogDescriptor(Bundle.addCompositeCalendar_dialog_title());
        if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION) {
            List<WeightedItem<String>> weightedItems = panel.getWeightedItems();
            CompositeCalendar newObj = new CompositeCalendar(weightedItems.toArray(new WeightedItem[weightedItems.size()]));
            add(manager, panel.getCalendarName(), newObj);
        }
    }
}
