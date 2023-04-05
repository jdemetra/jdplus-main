/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui.calendar;

import jdplus.toolkit.desktop.plugin.properties.NodePropertySetBuilder;
import jdplus.toolkit.base.api.timeseries.calendars.DayEvent;
import org.openide.nodes.Sheet;

/**
 *
 * @author Philippe Charles
 */
public class SpecialEventNode extends AbstractEventNode {

    public SpecialEventNode(PrespecifiedHolidayBean bean) {
        super(bean);
    }

    @Override
    public String getHtmlDisplayName() {
        PrespecifiedHolidayBean bean = getLookup().lookup(PrespecifiedHolidayBean.class);
        StringBuilder sb = new StringBuilder();
        sb.append("<b>Special Day</b> ");
        sb.append(bean.getDayEvent()).append(", ").append(bean.getOffset());
        return sb.toString();
    }

    @Override
    protected Sheet createSheet() {
        PrespecifiedHolidayBean bean = getLookup().lookup(PrespecifiedHolidayBean.class);
        Sheet result = super.createSheet();
        NodePropertySetBuilder b = new NodePropertySetBuilder().name("Special Day");
        b.withEnum(DayEvent.class).select(bean, PrespecifiedHolidayBean.DAY_EVENT_PROPERTY).display("Day event").add();
        b.withInt().select(bean, PrespecifiedHolidayBean.OFFSET_PROPERTY).display("Offset").add();
        result.put(b.build());
        return result;
    }
}
