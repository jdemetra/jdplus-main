/*
 * Copyright 2022 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.sa.desktop.plugin.html;

import java.io.IOException;
import jdplus.sa.base.core.tests.CombinedSeasonality;
import jdplus.sa.base.core.tests.Friedman;
import jdplus.sa.base.core.tests.KruskalWallis;
import jdplus.toolkit.base.api.stats.OneWayAnova;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.core.stats.tests.TestsUtility;
import jdplus.toolkit.desktop.plugin.html.AbstractHtmlElement;
import jdplus.toolkit.desktop.plugin.html.Bootstrap4;
import jdplus.toolkit.desktop.plugin.html.HtmlClass;
import jdplus.toolkit.desktop.plugin.html.HtmlElement;
import jdplus.toolkit.desktop.plugin.html.HtmlStream;
import jdplus.toolkit.desktop.plugin.html.HtmlTable;
import jdplus.toolkit.desktop.plugin.html.HtmlTableCell;
import jdplus.toolkit.desktop.plugin.html.HtmlTag;

/**
 *
 * @author palatej
 */
public class HtmlCombinedSeasonalityTest extends AbstractHtmlElement implements HtmlElement {

    private TsData si;
    private CombinedSeasonality combinedTest;
    private Friedman friedmanTest;
    private double m_badthreshold = 0.1;
    private double m_goodthresohold = 0.01;
 
    public HtmlCombinedSeasonalityTest(TsData si, boolean mul) {
        if (si == null || si.getAnnualFrequency()<2) {
            return;
        }
        this.si = si;
        combinedTest = CombinedSeasonality.of(si, mul ? 1 : 0);
        friedmanTest = new Friedman(si.getValues(), si.getAnnualFrequency());

    }

    /**
     *
     * @param stream
     * @throws IOException
     */
    public void CombinedSeasonalityTest(HtmlStream stream) throws IOException {

        stream.write(HtmlTag.HEADER1, "Combined seasonality test")
                .newLine();
        switch (combinedTest.getSummary()) {
            case None -> stream.write(HtmlTag.IMPORTANT_TEXT, "Identifiable seasonality not present", Bootstrap4.TEXT_DANGER);
            case ProbablyNone -> stream.write("Identifiable seasonality probably not present", Bootstrap4.TEXT_WARNING);
            case Present -> stream.write("Identifiable seasonality present", Bootstrap4.TEXT_SUCCESS);
        }
        stream.newLines(2);
    }

    /**
     *
     * @param stream
     * @throws IOException
     */
    public void EvolutionSeasonalityTest(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER1, "Evolutive seasonality test")
                .newLine();
        OneWayAnova stest = combinedTest.getEvolutiveSeasonalityTest();
        stream.open(new HtmlTable().withWidth(400));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("").withWidth(100));
        stream.write(new HtmlTableCell("Sum of squares").withWidth(100));
        stream.write(new HtmlTableCell("Degrees of freedom").withWidth(100));
        stream.write(new HtmlTableCell("Mean square").withWidth(100));
        stream.close(HtmlTag.TABLEROW);

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Between years").withWidth(100).withClass(Bootstrap4.FONT_ITALIC));
        stream.write(new HtmlTableCell(Double.toString(stest.getSsm())).withWidth(100));
        stream.write(new HtmlTableCell(Double.toString(stest.getDfm())).withWidth(100));
        stream.write(new HtmlTableCell(Double.toString(stest.getSsm()/ stest.getDfm())).withWidth(100));
        stream.close(HtmlTag.TABLEROW);

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Error").withWidth(100).withClass(Bootstrap4.FONT_ITALIC));
        stream.write(new HtmlTableCell(Double.toString(stest.getSsr())).withWidth(100));
        stream.write(new HtmlTableCell(Double.toString(stest.getDfr())).withWidth(100));
        stream.write(new HtmlTableCell(Double.toString(stest.getSsr() / stest.getDfr())).withWidth(100));
        stream.close(HtmlTag.TABLE).newLines(2);

        
        StatisticalTest etest = TestsUtility.ofAnova(stest);
        stream.write("Value: " + etest.getValue()).newLine();
        stream.write(
                "Distribution: " + etest.getDescription())
                .newLine();
        stream.write("PValue: " + df4.format(etest.getPvalue()),
                getRPvalueClass(etest.getPvalue(), 0.01, 0.05)).newLine();
        if (etest.getPvalue() > 0.2) {
            stream
                    .write(
                            "No evidence of moving seasonality at the 20 per cent level")
                    .newLines(2);
        } else if (etest.getPvalue() < 0.05) {
            stream.write(HtmlTag.IMPORTANT_TEXT, "Moving seasonality present at the 5 per cent level").newLines(2);
        }
    }

    /**
     *
     * @param stream
     * @throws IOException
     */
    public void FKWTests(HtmlStream stream) throws IOException {
        StatisticalTest ftest = friedmanTest.build();
        stream.write(HtmlTag.HEADER1,
                "Non parametric tests for stable seasonality").newLine();
        stream.write(HtmlTag.HEADER2, "Friedman test").newLine();
        stream.write("Friedman statistic = " + df4.format(ftest.getValue()))
                .newLine();
        stream.write("Distribution: " + ftest.getDescription())
                .newLine();
        stream.write("P-Value: ").write(df4.format(ftest.getPvalue()),
                getPvalueClass(ftest.getPvalue())).newLines(2);
        if (ftest.getPvalue() < 0.01) {
            stream.write("Stable seasonality present at the 1 per cent level")
                    .newLines(2);
        } else if (ftest.getPvalue() > 0.05) {
            stream
                    .write(
                            "No evidence of stable seasonality at the 5 per cent level")
                    .newLines(2);
        }

        stream.write(HtmlTag.HEADER2, "Kruskal-Wallis test").newLine();
        KruskalWallis kw = combinedTest
                .getNonParametricTestForStableSeasonality();
        StatisticalTest ktest = kw.build();
        stream.write(
                        "Kruskal-Wallis statistic = "
                        + Double.toString(ktest.getValue())).newLine();
        stream.write("Distribution: " + ktest.getDescription())
                .newLine();
        stream.write("P-Value: ").write(df4.format(ktest.getPvalue()),
                getPvalueClass(ktest.getPvalue())).newLine();
        if (ktest.getPvalue() < 0.01) {
            stream.write("Stable seasonality present at the 1 per cent level")
                    .newLines(2);
        } else if (ktest.getPvalue() > 0.05) {
            stream
                    .write(
                            "No evidence of stable seasonality at the 5 per cent level")
                    .newLines(2);
        }
    }

    private HtmlClass getPvalueClass(double val) {
        if (val > m_badthreshold) {
            return Bootstrap4.TEXT_DANGER;
        } else if (val > m_goodthresohold) {
            return Bootstrap4.TEXT_WARNING;
        } else {
            return Bootstrap4.TEXT_SUCCESS;
        }
    }

    private HtmlClass getRPvalueClass(double val, double lb, double ub) {
        if (val < ub) {
            return Bootstrap4.TEXT_DANGER;
        } else if (val < lb) {
            return Bootstrap4.TEXT_WARNING;
        } else {
            return Bootstrap4.TEXT_SUCCESS;
        }
    }

    /**
     *
     * @param stream
     * @throws IOException
     */
    public void StableSeasonalityTest(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER1, "Test for the presence of seasonality assuming stability")
                .newLine();
        OneWayAnova ssTest = combinedTest.getStableSeasonalityTest();
        StatisticalTest stest=TestsUtility.ofAnova(ssTest);

        stream.open(new HtmlTable().withWidth(400));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("").withWidth(100));
        stream.write(new HtmlTableCell("Sum of squares").withWidth(100));
        stream.write(new HtmlTableCell("Degrees of freedom").withWidth(100));
        stream.write(new HtmlTableCell("Mean square").withWidth(100));
        stream.close(HtmlTag.TABLEROW);

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Between periods").withWidth(100).withClass(Bootstrap4.FONT_ITALIC));
        stream.write(new HtmlTableCell(Double.toString(ssTest.getSsm())).withWidth(100));
        stream.write(new HtmlTableCell(Double.toString(ssTest.getDfm())).withWidth(100));
        stream.write(new HtmlTableCell(Double.toString(ssTest.getSsm() / ssTest.getDfm())).withWidth(100));
        stream.close(HtmlTag.TABLEROW);

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Residual").withWidth(100).withClass(Bootstrap4.FONT_ITALIC));
        stream.write(new HtmlTableCell(Double.toString(ssTest.getSsr())).withWidth(100));
        stream.write(new HtmlTableCell(Double.toString(ssTest.getDfr())).withWidth(100));
        stream.write(new HtmlTableCell(Double.toString(ssTest.getSsr() / ssTest.getDfr())).withWidth(100));
        stream.close(HtmlTag.TABLEROW);

        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Total").withWidth(100).withClass(Bootstrap4.FONT_ITALIC));
        stream.write(new HtmlTableCell(Double.toString(ssTest.getSsq())).withWidth(100));
        stream.write(new HtmlTableCell(Double.toString(ssTest.getDfq())).withWidth(100));
        stream.write(new HtmlTableCell(Double.toString(ssTest.getSsq() / ssTest.getDfq())).withWidth(100));
        stream.close(HtmlTag.TABLEROW);
        stream.close(HtmlTag.TABLE).newLine();

        stream.write("Value: " + stest.getValue()).newLine();
        stream.write(
                "Distribution: " + stest.getDescription())
                .newLine();
        stream.write("PValue: " + df4.format(stest.getPvalue()),
                getPvalueClass(stest.getPvalue())).newLine();
        if (stest.getPvalue() < 0.01) {
            stream.write("Seasonality present at the 1 per cent level")
                    .newLines(2);
        } else if (stest.getPvalue() > 0.05) {
            stream.write(HtmlTag.IMPORTANT_TEXT, "No evidence of seasonality at the 5 per cent level").newLines(2);
        }
    }

    /**
     *
     * @param stream
     * @throws IOException
     */
    @Override
    public void write(HtmlStream stream) throws IOException {
        if (si == null) {
            stream.write("Series can't be tested");
            return;
        }
        FKWTests(stream);
        StableSeasonalityTest(stream);
        EvolutionSeasonalityTest(stream);
        CombinedSeasonalityTest(stream);
//        ResidualSeasonality(stream);
    }
}

