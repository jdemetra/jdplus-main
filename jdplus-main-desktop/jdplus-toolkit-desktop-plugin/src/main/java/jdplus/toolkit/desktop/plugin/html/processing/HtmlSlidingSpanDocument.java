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
import static jdplus.toolkit.desktop.plugin.html.Bootstrap4.FONT_ITALIC;
import jdplus.toolkit.desktop.plugin.html.HtmlClass;
import jdplus.toolkit.desktop.plugin.html.HtmlElement;
import jdplus.toolkit.desktop.plugin.html.HtmlStream;
import jdplus.toolkit.desktop.plugin.html.HtmlTable;
import jdplus.toolkit.desktop.plugin.html.HtmlTableCell;
import jdplus.toolkit.desktop.plugin.html.HtmlTag;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.calendars.CalendarUtility;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Function;
import jdplus.toolkit.base.core.stats.DescriptiveStatistics;
import jdplus.toolkit.base.core.timeseries.simplets.PeriodIterator;
import jdplus.toolkit.base.core.timeseries.simplets.TsDataView;
import jdplus.toolkit.base.core.timeseries.simplets.YearIterator;
import jdplus.toolkit.base.core.timeseries.simplets.analysis.DiagnosticInfo;
import jdplus.toolkit.base.core.timeseries.simplets.analysis.SlidingSpans;

/**
 *
 * @author Kristof Bayens
 */
public class HtmlSlidingSpanDocument<I> extends AbstractHtmlElement implements HtmlElement {

    private final SlidingSpans<I> slidingspans;
    private final Function<I, TsData> extractor;
    private final DiagnosticInfo info;
    private double threshold_ = 0.03;

    public HtmlSlidingSpanDocument(SlidingSpans slidingspans, DiagnosticInfo info, Function<I, TsData> extractor) {
        this.slidingspans = slidingspans;
        this.extractor = extractor;
        this.info = info;
    }

    /**
     *
     * @param stream
     * @throws IOException
     */
    @Override
    public void write(HtmlStream stream) throws IOException {
        TsData s = slidingspans.statistics(info, extractor);
        if (s == null || s.getValues().count(x->Double.isNaN(x))== s.length())
            return;

        DescriptiveStatistics stats = DescriptiveStatistics.of(s.getValues());
        if (stats.getMax() == stats.getMin() || stats.getMax() == 0)
            return;

        stream.write("Abnormal values : ");

        NumberFormat format = new DecimalFormat("0.0", DecimalFormatSymbols.getInstance(Locale.getDefault(Locale.Category.FORMAT)));
        int nabnormal = stats.countBetween(threshold_, Double.MAX_VALUE);
        double mabnormal = stats.getAverage();
        double p = nabnormal * 100.0 / stats.getObservationsCount();
        stream.write(format.format(p) + "%").newLines(2);

        if (nabnormal != 0) {
            String title;
            if (info == DiagnosticInfo.AbsoluteDifference || info == DiagnosticInfo.PeriodToPeriodDifference)
                title = "Breakdowns of unstable factors and Average Maximum Differences across spans";
            else
                title = "Breakdowns of unstable factors and Average Maximum Percent Differences across spans";
            stream.write(HtmlTag.IMPORTANT_TEXT, title, Bootstrap4.TEXT_INFO).newLines(2);

            PeriodIterator iter = new PeriodIterator(s);
            int freq = s.getAnnualFrequency();

            stream.open(new HtmlTable().withWidth(300));
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Period").withClass(FONT_ITALIC));
            stream.write(new HtmlTableCell("Breakdowns").withClass(FONT_ITALIC));
            stream.write(new HtmlTableCell("Average").withClass(FONT_ITALIC));
            stream.close(HtmlTag.TABLEROW);

            double nbold = 2 * nabnormal;
            nbold /= freq;

            while(iter.hasNext()) {
                TsDataView block = iter.next();
                DescriptiveStatistics desc = DescriptiveStatistics.of(block.getData());
                int n = desc.countBetween(threshold_, Double.MAX_VALUE);
                double m = desc.getAverage();
                boolean bold = n > nbold | m > 2 * mabnormal;
                HtmlClass style = bold ? Bootstrap4.FONT_WEIGHT_BOLD : HtmlClass.NO_CLASS;

                stream.open(HtmlTag.TABLEROW);
                stream.write(new HtmlTableCell(CalendarUtility.formatPeriod(freq, block.getStart().annualPosition())).withClass(style));
                stream.write(new HtmlTableCell(n + "").withClass(style));
                if (info == DiagnosticInfo.AbsoluteDifference || info == DiagnosticInfo.PeriodToPeriodDifference)
                    stream.write(new HtmlTableCell(format.format(m)).withClass(style));
                else
                    stream.write(new HtmlTableCell(format.format(100 * m)).withClass(style));
                stream.close(HtmlTag.TABLEROW);
            }
            stream.close(HtmlTag.TABLE).newLines(2);

            int y0 = s.getDomain().getStartPeriod().year();
            int y1 = s.getDomain().getLastPeriod().year();
            stream.open(new HtmlTable().withWidth(300));
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Year").withClass(FONT_ITALIC));
            stream.write(new HtmlTableCell("Breakdowns").withClass(FONT_ITALIC));
            stream.write(new HtmlTableCell("Average").withClass(FONT_ITALIC));
            stream.close(HtmlTag.TABLEROW);

            nbold = 2 * nabnormal;
            nbold /= (y1-y0+1);

            
            YearIterator yiter = new YearIterator(s);
            while(yiter.hasNext()) {
                TsDataView block = yiter.next();
                DescriptiveStatistics desc = DescriptiveStatistics.of(block.getData());
                int n = desc.countBetween(threshold_, Double.MAX_VALUE);
                double m = desc.getAverage();
                boolean bold = n > nbold | m > 2 * mabnormal;
                HtmlClass style = bold ? Bootstrap4.FONT_WEIGHT_BOLD : HtmlClass.NO_CLASS;

                stream.open(HtmlTag.TABLEROW);
                stream.write(new HtmlTableCell(block.getStart().year() + "").withClass(style));
                stream.write(new HtmlTableCell(n + "").withClass(style));
                if (info == DiagnosticInfo.AbsoluteDifference || info == DiagnosticInfo.PeriodToPeriodDifference)
                    stream.write(new HtmlTableCell(format.format(m)).withClass(style));
                else
                    stream.write(new HtmlTableCell(format.format(100 * m)).withClass(style));
                stream.close(HtmlTag.TABLEROW);
            }
            stream.close(HtmlTag.TABLE);
        }
    }

    public double getThreshold() {
        return threshold_;
    }
    public void setThreshold(double threshold) {
        threshold_ = threshold;
    }
}
