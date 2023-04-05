/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui.properties.l2fprod;

import com.l2fprod.common.beans.editor.ComboBoxPropertyEditor;
import jdplus.toolkit.base.api.timeseries.calendars.CalendarManager;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import java.awt.Component;

/**
 *
 * @author Jean Palate
 */
public class HolidaysSelector extends ComboBoxPropertyEditor {

    public HolidaysSelector() {
    }

    @Override
    public Component getCustomEditor() {
        CalendarManager mgr = ModellingContext.getActiveContext().getCalendars();
        String[] names = mgr.getNames();
        Value[] values = new Value[mgr.getCount()];
        for (int i = 0; i < values.length; i++) {
            values[i] = new Value(new Holidays(names[i]), names[i]);
        }
        setAvailableValues(values);
        return super.getCustomEditor();
    }
}
