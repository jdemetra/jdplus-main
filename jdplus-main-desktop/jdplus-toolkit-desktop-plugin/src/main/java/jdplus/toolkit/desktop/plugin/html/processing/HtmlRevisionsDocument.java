/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
package jdplus.toolkit.desktop.plugin.html.processing;

import jdplus.toolkit.desktop.plugin.html.AbstractHtmlElement;
import jdplus.toolkit.desktop.plugin.html.Bootstrap4;
import static jdplus.toolkit.desktop.plugin.html.Bootstrap4.FONT_WEIGHT_BOLD;
import jdplus.toolkit.desktop.plugin.html.HtmlElement;
import jdplus.toolkit.desktop.plugin.html.HtmlStream;
import jdplus.toolkit.desktop.plugin.html.HtmlTable;
import jdplus.toolkit.desktop.plugin.html.HtmlTableCell;
import jdplus.toolkit.desktop.plugin.html.HtmlTableHeader;
import jdplus.toolkit.desktop.plugin.html.HtmlTag;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

import jdplus.toolkit.base.core.stats.DescriptiveStatistics;
import jdplus.toolkit.base.core.timeseries.simplets.analysis.DiagnosticInfo;

/**
 *
 * @author Kristof Bayens
 */
public class HtmlRevisionsDocument extends AbstractHtmlElement implements HtmlElement {

    private final TsData revisions_;
    private final DiagnosticInfo info_;
    private int threshold_ = 2;

    public HtmlRevisionsDocument(TsData revisions, DiagnosticInfo info) {
        revisions_ = revisions;
        info_ = info;
    }

    /**
     *
     * @param stream
     * @throws IOException
     */
    @Override
    public void write(HtmlStream stream) throws IOException {

        stream.write(HtmlTag.HEADER1, info_.toString());

        int y0 = revisions_.getDomain().getStartPeriod().year();
        int y1 = revisions_.getDomain().getLastPeriod().year();

        DescriptiveStatistics stats = DescriptiveStatistics.of(revisions_.getValues());

        double mean = stats.getAverage();
        double rmse = stats.getRmse();

        NumberFormat format = new DecimalFormat("0.0000", DecimalFormatSymbols.getInstance(Locale.getDefault(Locale.Category.FORMAT)));
        stream.write(HtmlTag.EMPHASIZED_TEXT, "mean = " + format.format(mean)).newLine();
        stream.write(HtmlTag.EMPHASIZED_TEXT, "rmse = " + format.format(rmse)).newLines(2);

        int columnscount = 2 + y1 - y0;
        stream.open(new HtmlTable().withWidth(400));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell(""));
        for (int i = y0; i <= y1; ++i) {
            stream.write(new HtmlTableHeader("" + i));
        }
        stream.close(HtmlTag.TABLEROW);

        format = new DecimalFormat("0.000", DecimalFormatSymbols.getInstance(Locale.getDefault(Locale.Category.FORMAT)));
        TsPeriod x = revisions_.getStart().withDate(LocalDate.of(y0, 1,1).atStartOfDay());
        for (int i = 0; i < revisions_.getAnnualFrequency(); ++i) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell(Integer.toString(x.annualPosition()+1)).withClass(FONT_WEIGHT_BOLD));
            for (int j = 1; j < columnscount; ++j) {
                String txt = "";
                boolean danger = false;
                int k = revisions_.getDomain().indexOf(x.plus((j - 1) * revisions_.getAnnualFrequency()));
                if (k >= 0) {
                    double d = revisions_.getValue(k);
                    if (Math.abs(d) > threshold_ * rmse) {
                        danger = true;
                    }
                    txt = format.format(revisions_.getValue(k));
                }
                if (danger) {
                    stream.write(new HtmlTableCell(txt).withClass(Bootstrap4.TEXT_DANGER));
                } else {
                    stream.write(new HtmlTableCell(txt));
                }
            }
            stream.close(HtmlTag.TABLEROW);
            x = x.plus(1);
        }
        stream.close(HtmlTag.TABLE);
    }

    public int getThreshold() {
        return threshold_;
    }

    public void setThreshold(int threshold) {
        threshold_ = threshold;
    }
}
