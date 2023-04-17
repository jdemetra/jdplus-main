/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui.calendar;

import jdplus.toolkit.desktop.plugin.properties.NodePropertySetBuilder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.openide.nodes.Sheet;

/**
 *
 * @author Philippe Charles
 */
public class EasterRelatedEventNode extends AbstractEventNode {

    public EasterRelatedEventNode(EasterRelatedEventBean bean) {
        super(bean);
    }

    @Override
    public String getHtmlDisplayName() {
        EasterRelatedEventBean bean = getLookup().lookup(EasterRelatedEventBean.class);
        StringBuilder sb = new StringBuilder();
        sb.append("<b>Easter</b> ");
        DecimalFormat df = new DecimalFormat("+#;-#", DecimalFormatSymbols.getInstance(Locale.getDefault(Locale.Category.FORMAT)));
        sb.append(df.format(bean.getOffset()));
        return sb.toString();
    }

    @Override
    protected Sheet createSheet() {
        EasterRelatedEventBean bean = getLookup().lookup(EasterRelatedEventBean.class);
        Sheet result = super.createSheet();
        NodePropertySetBuilder b = new NodePropertySetBuilder().name("Easter Related Day");
        b.withInt().select(bean, EasterRelatedEventBean.OFFSET_PROPERTY).min(-366).max(366).display("Offset").add();
        b.withBoolean().select(bean, EasterRelatedEventBean.JULIAN_PROPERTY).display("Julian").add();
        result.put(b.build());
        return result;
    }
}
