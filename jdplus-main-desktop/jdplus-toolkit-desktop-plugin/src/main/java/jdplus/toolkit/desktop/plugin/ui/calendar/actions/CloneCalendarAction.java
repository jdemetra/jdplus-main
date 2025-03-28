/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui.calendar.actions;

import jdplus.toolkit.desktop.plugin.nodes.SingleNodeAction;
import jdplus.toolkit.desktop.plugin.ui.calendar.ChainedGregorianCalendarPanel;
import jdplus.toolkit.desktop.plugin.ui.calendar.CompositeGregorianCalendarPanel;
import jdplus.toolkit.desktop.plugin.ui.calendar.NationalCalendarPanel;
import jdplus.toolkit.desktop.plugin.workspace.CalendarDocumentManager;
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

import java.util.Arrays;
import java.util.List;

@ActionID(category = "Tools", id = CloneCalendarAction.ID)
@ActionRegistration(displayName = "#CTL_CloneCalendarAction", lazy = false)
@ActionReferences({
    @ActionReference(path = CalendarDocumentManager.ITEMPATH, position = 1421, separatorBefore = 1400)
})
@Messages("CTL_CloneCalendarAction=Clone")
public final class CloneCalendarAction extends SingleNodeAction<ItemWsNode> {

    @ClassNameConstant
    public static final String ID = "jdplus.toolkit.desktop.plugin.ui.calendar.actions.CloneCalendarAction";

    public CloneCalendarAction() {
        super(ItemWsNode.class);
    }

    @Override
    protected void performAction(ItemWsNode activatedNode) {
        CalendarManager manager = ModellingContext.getActiveContext().getCalendars();
        CalendarDefinition o = AddCalendarAction.getProvider(activatedNode);
        if (o instanceof Calendar) {
            cloneNationalCalendar(manager, (Calendar) o, activatedNode);
        } else if (o instanceof ChainedCalendar) {
            cloneChainedCalendar(manager, (ChainedCalendar) o, activatedNode);
        } else if (o instanceof CompositeCalendar) {
            cloneCompositeCalendar(manager, (CompositeCalendar) o, activatedNode);
        }
    }

    @Override
    protected boolean enable(ItemWsNode activatedNode) {
        CalendarDefinition o = AddCalendarAction.getProvider(activatedNode);
        return o instanceof Calendar
                || o instanceof ChainedCalendar
                || o instanceof CompositeCalendar;
    }

    @Override
    public String getName() {
        return Bundle.CTL_CloneCalendarAction();
    }

    @Messages({
        "cloneNationalCalendar.dialog.title=Clone National Calendar"
    })
    static void cloneNationalCalendar(CalendarManager manager, Calendar p, ItemWsNode node) {
        NationalCalendarPanel panel = new NationalCalendarPanel();
        panel.setHolidays(Arrays.asList(p.getHolidays()));

        DialogDescriptor dd = panel.createDialogDescriptor(Bundle.cloneNationalCalendar_dialog_title());
        if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION) {
            List<Holiday> holidays = panel.getHolidays();
            Calendar newObject = new Calendar(holidays.toArray(Holiday[]::new));
            AddCalendarAction.add(manager, panel.getCalendarName(), newObject);
        }
    }

    @Messages({
        "cloneChainedCalendar.dialog.title=Clone Chained Calendar"
    })
    static void cloneChainedCalendar(CalendarManager manager, ChainedCalendar p, ItemWsNode node) {
        ChainedGregorianCalendarPanel panel = new ChainedGregorianCalendarPanel();
        panel.setFirstCalendar(p.getFirst());
        panel.setDayBreak(p.getBreakDate());
        panel.setSecondCalendar(p.getSecond());

        DialogDescriptor dd = panel.createDialogDescriptor(Bundle.cloneChainedCalendar_dialog_title());
        if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION) {
            ChainedCalendar newObject = new ChainedCalendar(panel.getFirstCalendar(), panel.getSecondCalendar(), panel.getDayBreak());
            AddCalendarAction.add(manager, panel.getCalendarName(), newObject);
        }
    }

    @Messages({
        "cloneCompositeCalendar.dialog.title=Clone Composite Calendar"
    })
    static void cloneCompositeCalendar(CalendarManager manager, CompositeCalendar p, ItemWsNode node) {
        CompositeGregorianCalendarPanel panel = new CompositeGregorianCalendarPanel("");
        panel.setWeightedItems(Arrays.asList(p.getCalendars()));

        DialogDescriptor dd = panel.createDialogDescriptor(Bundle.cloneCompositeCalendar_dialog_title());
        if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION) {
            String name = panel.getCalendarName();
            List<WeightedItem<String>> weightedItems = panel.getWeightedItems();
            CompositeCalendar newObj = new CompositeCalendar(weightedItems.toArray(WeightedItem[]::new));
            AddCalendarAction.add(manager, name, newObj);
        }
    }
}
