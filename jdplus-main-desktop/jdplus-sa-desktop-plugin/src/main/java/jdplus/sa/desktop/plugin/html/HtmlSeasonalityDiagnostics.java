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
package jdplus.sa.desktop.plugin.html;

import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.desktop.plugin.html.AbstractHtmlElement;
import jdplus.toolkit.desktop.plugin.html.Bootstrap4;
import jdplus.toolkit.desktop.plugin.html.HtmlClass;
import jdplus.toolkit.desktop.plugin.html.HtmlElement;
import jdplus.toolkit.desktop.plugin.html.HtmlStream;
import jdplus.toolkit.desktop.plugin.html.HtmlTable;
import jdplus.toolkit.desktop.plugin.html.HtmlTableCell;
import jdplus.toolkit.desktop.plugin.html.HtmlTag;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import java.io.IOException;
import java.util.function.IntToDoubleFunction;
import jdplus.sa.base.core.diagnostics.GenericSeasonalityTests;
import jdplus.sa.base.core.diagnostics.ResidualSeasonalityTests;
import jdplus.sa.base.core.tests.SpectralPeaks;
import jdplus.toolkit.base.api.stats.AutoCovariances;
import jdplus.toolkit.base.core.modelling.DifferencingResult;

/**
 *
 * @author Jean Palate
 */
public class HtmlSeasonalityDiagnostics extends AbstractHtmlElement implements HtmlElement {
    private final GenericSeasonalityTests tests;
    private final boolean noSeasControl;

    public HtmlSeasonalityDiagnostics(final GenericSeasonalityTests tests) {
        this(tests, false);
    }

    public HtmlSeasonalityDiagnostics(final GenericSeasonalityTests tests, final boolean noSeasControl) {
        this.tests = tests;
        this.noSeasControl = noSeasControl;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        if (tests != null) {
            writeSummary(stream);
            writeQS(stream);
            writeFriedman(stream);
            writeKruskalWallis(stream);
            writeSpectrum(stream);
            writePeriodogram(stream);
            writeFTest(stream);
        } else {
            stream.write("Series can't be tested");
        }
    }

    public void writeTransformation(HtmlStream stream) throws IOException {
        if (tests.getDifferencing().isMeanCorrection() && tests.getDifferencing().getDifferencingOrder() == 1) {
            stream.write(HtmlTag.EMPHASIZED_TEXT, "Data have been differenced and corrected for mean").newLines(2);
        } else if (tests.getDifferencing().getDifferencingOrder() > 0) {
            stream.write(HtmlTag.EMPHASIZED_TEXT, "Data have been differenced " + tests.getDifferencing().getDifferencingOrder() + " times").newLine();
            if (tests.getDifferencing().isMeanCorrection()) {
                stream.write(HtmlTag.EMPHASIZED_TEXT, "Data have been corrected for mean").newLine();
            }
            stream.newLine();
        }
    }

    public void writeQS(HtmlStream stream) throws IOException {
        StatisticalTest test = tests.qsTest();
        if (test == null) {
            return;
        }
        stream.write(HtmlTag.HEADER4, "1. Tests on autocorrelations at seasonal lags").newLine();
        writeSummary(stream, test.getPvalue());
        stream.newLines(2);
        DifferencingResult differencing = tests.getDifferencing();
        int period = tests.getSeries().getAnnualFrequency();
        IntToDoubleFunction ac = AutoCovariances.autoCorrelationFunction(differencing.getDifferenced(), 0);
        stream.write("ac(").write(period).write(")=").write(df4.format(ac.applyAsDouble(period))).newLine();
        stream.write("ac(").write(2 * period).write(")=").write(df4.format(ac.applyAsDouble(2 * period))).newLines(2);
        stream.write("Distribution: " + test.getDescription()).newLine();
        stream.write("Value: " + df4.format(test.getValue())).newLine();
        stream.write("PValue: " + df4.format(test.getPvalue()));
        stream.write(HtmlTag.LINEBREAK);
    }

    public void writeFTest(HtmlStream stream) throws IOException {
        StatisticalTest test = tests.fTest();
        if (test == null) {
            return;
        }
        stream.write(HtmlTag.HEADER4, "6. Tests on regression with fixed seasonal dummies ").newLine();
        stream.write(HtmlTag.EMPHASIZED_TEXT, "Data have been differenced and corrected for mean").newLine();
        writeSummary(stream, test.getPvalue());
        stream.newLines(2);
        stream.write("Distribution: " + test.getDescription()).newLine();
        stream.write("Value: " + df4.format(test.getValue())).newLine();
        stream.write("PValue: " + df4.format(test.getPvalue()));
        stream.write(HtmlTag.LINEBREAK);
    }

    public void writeFriedman(HtmlStream stream) throws IOException {
        StatisticalTest test = tests.friedmanTest();
        if (test == null) {
            return;
        }
        stream.write(HtmlTag.HEADER4, "2. Non parametric (Friedman) test");
        stream.write(HtmlTag.EMPHASIZED_TEXT, "Based on the rank of the observations in each year").newLines(2);
        writeSummary(stream, test.getPvalue());
        stream.newLine();
        stream.write("Distribution: " + test.getDescription()).newLine();
        stream.write("Value: " + df4.format(test.getValue())).newLine();
        stream.write("PValue: " + df4.format(test.getPvalue()));
        stream.write(HtmlTag.LINEBREAK);
    }

    public void writeKruskalWallis(HtmlStream stream) throws IOException {
        StatisticalTest test = tests.kruskalWallisTest();
        if (test == null) {
            return;
        }
        stream.write(HtmlTag.HEADER4, "3. Non parametric (Kruskal-Wallis) test");
        stream.write(HtmlTag.EMPHASIZED_TEXT, "Based on the rank of the observations").newLines(2);
        writeSummary(stream, test.getPvalue());
        stream.newLine();
        stream.write("Distribution: " + test.getDescription()).newLine();
        stream.write("Value: " + df4.format(test.getValue())).newLine();
        stream.write("PValue: " + df4.format(test.getPvalue()));
        stream.write(HtmlTag.LINEBREAK);
    }

    public void writeSpectrum(HtmlStream stream) throws IOException {
        SpectralPeaks[] spectralPeaks = tests.spectralPeaks();
        if (spectralPeaks == null) {
            return;
        }
        stream.write(HtmlTag.HEADER4, "4. Identification of seasonal peaks in a Tukey periodogram and in an auto-regressive spectrum");
        int diag = 1;
        if (SpectralPeaks.hasHighSeasonalPeaks(spectralPeaks)) {
            diag = -1;
        } else if (SpectralPeaks.hasSeasonalPeaks(spectralPeaks)) {
            diag = 0;
        }
        stream.newLine();
        writeSummary(stream, diag);
        stream.newLines(2);
        stream.write(HtmlTag.EMPHASIZED_TEXT, "T or t for Tukey periodogram, A or a for auto-regressive spectrum; 'T' or 'A' for very signficant peaks, 't' or 'a' for signficant peaks, '_' otherwise").newLines(2);
        stream.newLine();
        stream.write(SpectralPeaks.format(spectralPeaks));
        stream.write(HtmlTag.LINEBREAK);
    }

    public void writePeriodogram(HtmlStream stream) throws IOException {
        StatisticalTest test = tests.periodogramTest();
        if (test == null) {
            return;
        }
        stream.write(HtmlTag.HEADER4, "5. Periodogram");
        stream.write(HtmlTag.EMPHASIZED_TEXT, "Test on the sum of the values of a periodogram at seasonal frequencies").newLines(2);
        stream.newLine();
        writeSummary(stream, test.getPvalue());
        stream.newLines(2);
        stream.write("Distribution: " + test.getDescription()).newLine();
        stream.write("Value: " + df4.format(test.getValue())).newLine();
        stream.write("PValue: " + df4.format(test.getPvalue()));
        stream.write(HtmlTag.LINEBREAK);
    }

    private void writeSummary(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER4, "Summary");
        writeTransformation(stream);
        stream.open(new HtmlTable().withWidth(300));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Test").withWidth(250));
        stream.write(new HtmlTableCell("Seasonality").withWidth(50));
        stream.close(HtmlTag.TABLEROW);

        StatisticalTest test = tests.qsTest();
        if (test != null) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("1. Auto-correlations at seasonal lags").withWidth(250));
            stream.write(getCellSummary(test.getPvalue(), 50));
            stream.close(HtmlTag.TABLEROW);
        }

        test = tests.friedmanTest();
        if (test != null) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("2. Friedman (non parametric)").withWidth(250));
            stream.write(getCellSummary(test.getPvalue(), 50));
            stream.close(HtmlTag.TABLEROW);
        }

        test = tests.kruskalWallisTest();
        if (test != null) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("3. Kruskal-Wallis (non parametric)").withWidth(250));
            stream.write(getCellSummary(test.getPvalue(), 50));
            stream.close(HtmlTag.TABLEROW);
        }

        SpectralPeaks[] spectralPeaks = tests.spectralPeaks();
        if (spectralPeaks != null) {
            int diag = 1;
            if (SpectralPeaks.hasHighSeasonalPeaks(spectralPeaks)) {
                diag = -1;
            } else if (SpectralPeaks.hasSeasonalPeaks(spectralPeaks)) {
                diag = 0;
            }
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("4. Spectral peaks").withWidth(250));
            stream.write(getCellSummary(diag, 50));
            stream.close(HtmlTag.TABLEROW);
        }

        test = tests.periodogramTest();
        if (test != null) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("5. Periodogram ").withWidth(250));
            stream.write(getCellSummary(test.getPvalue(), 50));
            stream.close(HtmlTag.TABLEROW);
        }

        test = tests.fTest();
        if (test != null) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("6. Seasonal dummies").withWidth(250));
            stream.write(getCellSummary(test.getPvalue(), 50));
            stream.close(HtmlTag.TABLEROW);
            stream.open(HtmlTag.TABLEROW);
        }
        stream.close(HtmlTag.TABLE);
        stream.write(HtmlTag.LINEBREAK);
    }

    private void writeSummary(HtmlStream stream, double pval) throws IOException {
        int val = (pval > .05) ? 1 : ((pval > .01) ? 0 : -1);
        writeSummary(stream, val);
    }

    private void writeSummary(HtmlStream stream, int val) throws IOException {
        if (noSeasControl) {
            if (val < 0) {
                stream.write(HtmlTag.IMPORTANT_TEXT, "Seasonality present", Bootstrap4.TEXT_DANGER);
            } else if (val == 0) {
                stream.write("Seasonality perhaps present", Bootstrap4.TEXT_WARNING);
            } else {
                stream.write("Seasonality not present", Bootstrap4.TEXT_SUCCESS);
            }
        } else if (val < 0) {
            stream.write("Seasonality present", Bootstrap4.TEXT_SUCCESS);
        } else if (val == 0) {
            stream.write("Seasonality perhaps present", Bootstrap4.TEXT_WARNING);
        } else {
            stream.write(HtmlTag.IMPORTANT_TEXT, "Seasonality not present", Bootstrap4.TEXT_DANGER);
        }

    }

    private HtmlTableCell getCellSummary(double pval, int l) throws IOException {
        int val = (pval > .05) ? 1 : ((pval > .01) ? 0 : -1);
        return getCellSummary(val, l);
    }

    private HtmlTableCell getCellSummary(int val, int l) throws IOException {
        HtmlClass style;
        String txt;
        if (noSeasControl) {
            if (val < 0) {
                txt = "YES";
                style = Bootstrap4.TEXT_DANGER;
            } else if (val == 0) {
                txt = "?";
                style = Bootstrap4.TEXT_WARNING;
            } else {
                txt = "NO";
                style = Bootstrap4.TEXT_SUCCESS;
            }
        } else if (val < 0) {
            txt = "YES";
            style = Bootstrap4.TEXT_SUCCESS;
        } else if (val == 0) {
            txt = "?";
            style = Bootstrap4.TEXT_WARNING;
        } else {
            txt = "NO";
            style = Bootstrap4.TEXT_DANGER;
        }
        return new HtmlTableCell(txt).withWidth(l).withClass(style);
    }
}
