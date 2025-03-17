/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.desktop.plugin.x13.ui;

import jdplus.toolkit.desktop.plugin.TsDynamicProvider;
import jdplus.toolkit.desktop.plugin.modelling.ForecastsFactory;
import jdplus.toolkit.desktop.plugin.modelling.InputFactory;
import jdplus.toolkit.desktop.plugin.modelling.LikelihoodFactory;
import jdplus.toolkit.desktop.plugin.modelling.ModelArimaFactory;
import jdplus.toolkit.desktop.plugin.modelling.ModelRegressorsFactory;
import jdplus.toolkit.desktop.plugin.modelling.NiidTestsFactory;
import jdplus.toolkit.desktop.plugin.modelling.OutOfSampleTestFactory;
import jdplus.sa.desktop.plugin.processing.BenchmarkingUI;
import jdplus.sa.desktop.plugin.ui.DemetraSaUI;
import jdplus.sa.desktop.plugin.ui.SaViews;
import jdplus.toolkit.desktop.plugin.ui.processing.ContextualChartUI;
import jdplus.toolkit.desktop.plugin.ui.processing.ContextualIds;
import jdplus.toolkit.desktop.plugin.ui.processing.ContextualTableUI;
import jdplus.toolkit.desktop.plugin.ui.processing.GenericTableUI;
import jdplus.toolkit.desktop.plugin.ui.processing.HtmlItemUI;
import jdplus.toolkit.desktop.plugin.ui.processing.IProcDocumentItemFactory;
import jdplus.toolkit.desktop.plugin.ui.processing.IProcDocumentViewFactory;
import jdplus.toolkit.desktop.plugin.ui.processing.ProcDocumentItemFactory;
import jdplus.toolkit.desktop.plugin.ui.processing.ProcDocumentViewFactory;
import jdplus.toolkit.desktop.plugin.ui.processing.stats.ResidualsDistUI;
import jdplus.toolkit.desktop.plugin.ui.processing.stats.ResidualsUI;
import jdplus.toolkit.desktop.plugin.ui.processing.stats.RevisionHistoryUI;
import jdplus.toolkit.desktop.plugin.ui.processing.stats.SlidingSpansUI;
import jdplus.toolkit.desktop.plugin.ui.processing.stats.SpectrumUI;
import jdplus.toolkit.desktop.plugin.ui.processing.stats.StabilityUI;
import jdplus.toolkit.desktop.plugin.html.AbstractHtmlElement;
import jdplus.toolkit.desktop.plugin.html.HtmlElement;
import jdplus.toolkit.desktop.plugin.html.HtmlElements;
import jdplus.toolkit.desktop.plugin.html.HtmlHeader;
import jdplus.toolkit.desktop.plugin.html.HtmlStream;
import jdplus.toolkit.desktop.plugin.html.HtmlTag;
import jdplus.toolkit.desktop.plugin.html.core.HtmlDiagnosticsSummary;
import jdplus.toolkit.base.api.information.BasicInformationExtractor;
import jdplus.toolkit.base.api.information.Explorable;
import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.toolkit.base.api.processing.ProcDiagnostic;
import jdplus.toolkit.desktop.plugin.html.core.HtmlInformationSet;
import jdplus.toolkit.desktop.plugin.html.modelling.HtmlRegSarima;
import jdplus.x13.base.api.regarima.RegArimaSpec;
import jdplus.sa.base.api.DecompositionMode;
import jdplus.sa.base.api.EstimationPolicyType;
import jdplus.sa.base.api.SaDictionaries;
import jdplus.sa.base.api.SaManager;
import jdplus.sa.base.api.SaProcessingFactory;
import jdplus.sa.desktop.plugin.html.HtmlSaSlidingSpanSummary;
import jdplus.sa.desktop.plugin.html.HtmlSeasonalityDiagnostics;
import jdplus.sa.desktop.plugin.html.HtmlCombinedSeasonalityTest;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDocument;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.dictionaries.Dictionary;
import jdplus.toolkit.base.api.dictionaries.RegressionDictionaries;
import jdplus.toolkit.base.api.util.Id;
import jdplus.toolkit.base.api.util.LinearId;
import jdplus.x13.base.api.x11.SeasonalFilterOption;
import jdplus.x13.base.api.x11.X11Dictionaries;
import jdplus.x13.base.api.x13.X13Dictionaries;
import jdplus.x13.base.api.x13.X13Spec;
import jdplus.x13.base.information.X13SpecMapping;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import jdplus.sa.base.core.SaBenchmarkingResults;
import jdplus.sa.base.core.tests.SeasonalityTests;
import jdplus.sa.desktop.plugin.processing.SiRatioUI;
import jdplus.toolkit.base.api.modelling.SeriesInfo;
import jdplus.toolkit.base.api.timeseries.TimeSelector;
import jdplus.toolkit.base.core.timeseries.simplets.analysis.DiagnosticInfo;
import jdplus.toolkit.base.core.timeseries.simplets.analysis.MovingProcessing;
import jdplus.toolkit.base.core.timeseries.simplets.analysis.RevisionHistory;
import jdplus.toolkit.base.core.timeseries.simplets.analysis.SlidingSpans;
import jdplus.toolkit.desktop.plugin.html.core.HtmlProcessingLog;
import jdplus.x13.base.api.regarima.BasicSpec;
import jdplus.x13.base.core.x11.X11Results;
import jdplus.x13.base.core.x13.X13Diagnostics;
import jdplus.x13.base.core.x13.X13Document;
import jdplus.x13.base.core.x13.X13Factory;
import jdplus.x13.base.core.x13.X13Kernel;
import jdplus.x13.base.core.x13.X13Results;
import jdplus.x13.base.core.x13.regarima.RegArimaFactory;
import jdplus.x13.base.core.x13.regarima.RegArimaKernel;
import jdplus.x13.desktop.plugin.html.HtmlMstatistics;
import jdplus.x13.desktop.plugin.html.HtmlX11Diagnostics;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
public class X13ViewFactory extends ProcDocumentViewFactory<X13Document> {

    public static final String X11 = "Decomposition (X11)";
    // X11 nodes
    public static final String A = "A-Table", B = "B-Table",
            C = "C-Table", D = "D-Table", D_FINAL = "D-Final-Table", E = "E-Table", M = "Quality measures", FINALFILTERS = "Final filters";
    public static final Id M_STATISTICS_SUMMARY = new LinearId(X11, M, SaViews.SUMMARY),
            M_STATISTICS_DETAILS = new LinearId(X11, M, SaViews.DETAILS),
            X11_FILTERS = new LinearId(X11, FINALFILTERS),
            A_TABLES = new LinearId(X11, A),
            B_TABLES = new LinearId(X11, B),
            C_TABLES = new LinearId(X11, C),
            D_TABLES = new LinearId(X11, D),
            D_FINAL_TABLES = new LinearId(X11, D_FINAL),
            E_TABLES = new LinearId(X11, E);

    private static final AtomicReference<IProcDocumentViewFactory<X13Document>> INSTANCE = new AtomicReference();

    private final static Function<X13Document, RegSarimaModel> MODELEXTRACTOR = source -> {
        X13Results tr = source.getResult();
        return tr == null ? null : tr.getPreprocessing();
    };

    private final static Function<X13Document, X13Document> VALIDEXTRACTOR = source -> {
        X13Results tr = source.getResult();
        if (tr == null) {
            return null;
        }
        return tr.isValid() ? source : null;
    };

    private final static Function<X13Document, TsData> RESEXTRACTOR = MODELEXTRACTOR
            .andThen(regarima -> regarima == null ? null : regarima.fullResiduals());

    public static IProcDocumentViewFactory<X13Document> getDefault() {
        IProcDocumentViewFactory<X13Document> fac = INSTANCE.get();
        if (fac == null) {
            fac = new X13ViewFactory();
            INSTANCE.lazySet(fac);
        }
        return fac;
    }

    private final static Function<X13Document, X11Results> DECOMPOSITIONEXTRACTOR = source -> {
        X13Results tr = source.getResult();
        return tr == null ? null : tr.getDecomposition();
    };

    private final static Function<X13Document, X13Diagnostics> DIAGSEXTRACTOR = source -> {
        X13Results tr = source.getResult();
        return tr == null ? null : tr.getDiagnostics();
    };

    public static void setDefault(IProcDocumentViewFactory<X13Document> factory) {
        INSTANCE.set(factory);
    }

    public X13ViewFactory() {
        registerFromLookup(X13Document.class);
    }

    @Override
    public Id getPreferredView() {
        return SaViews.MAIN_SUMMARY;
    }

//<editor-fold defaultstate="collapsed" desc="INPUT">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 1010)
    public static class SpecFactory extends ProcDocumentItemFactory<X13Document, HtmlElement> {

        public SpecFactory() {
            super(X13Document.class, SaViews.INPUT_SPEC,
                    (X13Document doc) -> {
                        InformationSet info = X13SpecMapping.write(doc.getSpecification(), doc.getInput().getData().getDomain(), true);
                        return new HtmlInformationSet(info);
                    },
                    new HtmlItemUI()
            );
        }

        @Override
        public int getPosition() {
            return 1010;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 1000)
    public static class Input extends InputFactory<X13Document> {

        public Input() {
            super(X13Document.class, SaViews.INPUT_SERIES);
        }

        @Override
        public int getPosition() {
            return 1000;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 1020)
    public static class LogFactory extends ProcDocumentItemFactory<X13Document, HtmlElement> {

        public LogFactory() {
            super(X13Document.class, SaViews.MAIN_LOG,
                    (X13Document doc) -> {
                        X13Results result = doc.getResult();
                        if (result == null) {
                            return null;
                        } else {
                            HtmlProcessingLog html = new HtmlProcessingLog(result.getLog());
                            html.displayInfos(true);
                            return html;
                        }
                    },
                    new HtmlItemUI()
            );
        }

        @Override
        public int getPosition() {
            return 1020;
        }
    }

//</editor-fold>
    private static String generateId(String id) {
        return generateId(id, id);
    }

    private static String generateId(String name, String id) {
        return TsDynamicProvider.CompositeTs.builder()
                .name(name)
                .back(id + SaDictionaries.BACKCAST)
                .now(id)
                .fore(id + SaDictionaries.FORECAST)
                .build().toString();
    }

    private static String generateSimpleId(String name, String id) {
        return TsDynamicProvider.CompositeTs.builder()
                .name(name)
                .now(id)
                .build().toString();
    }

    private static String nsuffix(String suffix, int n) {
        StringBuilder builder = new StringBuilder();
        return builder.append(suffix).append('(').append(n).append(')').toString();
    }

    private static String generateId(String name, String id, int nb, int nf) {
        TsDynamicProvider.CompositeTs.Builder builder = TsDynamicProvider.CompositeTs.builder()
                .name(name);
        if (nb != 0) {
            builder.back(id + nsuffix(SeriesInfo.B_SUFFIX, nb));
        }
        builder.now(id);
        if (nf != 0) {
            builder.fore(id + nsuffix(SeriesInfo.F_SUFFIX, nf));
        }
        return builder.build().toString();
    }

    public static String[] lowSeries(boolean x11) {
        if (x11) {
            return new String[]{
                generateSimpleId("Series", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, X11Dictionaries.B1)),
                generateSimpleId("Seasonally adjusted", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, X11Dictionaries.D11)),
                generateSimpleId("Trend", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, X11Dictionaries.D12))
            };
        } else {
            return new String[]{
                generateId("Series", SaDictionaries.Y),
                generateId("Seasonally adjusted", SaDictionaries.SA),
                generateId("Trend", SaDictionaries.T)
            };
        }
    }

    public static String[] highSeries(boolean x11, int nb, int nf) {

        if (x11) {
            return new String[]{
                generateSimpleId("Seasonal", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, X11Dictionaries.D10)),
                generateSimpleId("Irregular", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, X11Dictionaries.D13))
            };
        } else {
            return new String[]{
                generateId("Seasonal (component)", BasicInformationExtractor.concatenate(SaDictionaries.DECOMPOSITION, SaDictionaries.S_CMP)),
                generateId("Calendar effects", RegressionDictionaries.CAL, nb, nf),
                generateId("Irregular", SaDictionaries.I)
            };
        }
    }

    public static String[] finalSeries(boolean x11) {
        if (x11) {
            return new String[]{
                generateSimpleId("Series", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, X11Dictionaries.B1)),
                generateSimpleId("Seasonally adjusted", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, X11Dictionaries.D11)),
                generateSimpleId("Trend", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, X11Dictionaries.D12)),
                generateSimpleId("Seasonal", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, X11Dictionaries.D10)),
                generateSimpleId("Irregular", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, X11Dictionaries.D13))
            };
        } else {
            return new String[]{
                generateId("Series", SaDictionaries.Y),
                generateId("Seasonally adjusted", SaDictionaries.SA),
                generateId("Trend", SaDictionaries.T),
                generateId("Seasonal", SaDictionaries.S),
                generateId("Irregular", SaDictionaries.I)
            };
        }
    }

    //<editor-fold defaultstate="collapsed" desc="MAIN">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 2000)
    public static class MainSummaryFactory extends ProcDocumentItemFactory<X13Document, X13Document> {

        public MainSummaryFactory() {
            super(X13Document.class, SaViews.MAIN_SUMMARY, VALIDEXTRACTOR, new X13Summary());
        }

        @Override
        public int getPosition() {
            return 2000;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 2100)
    public static class MainLowChart extends ProcDocumentItemFactory<X13Document, ContextualIds<TsDocument>> {

        public MainLowChart() {
            super(X13Document.class, SaViews.MAIN_CHARTS_LOW, s -> {
                if (s.getResult() == null || !s.getResult().isValid()) {
                    return null;
                }
                boolean x11 = s.getResult().getPreprocessing() == null;
                return new ContextualIds<>(lowSeries(x11), s);
            }, new ContextualChartUI(true));
        }

        @Override
        public int getPosition() {
            return 2100;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 2200)
    public static class MainHighChart extends ProcDocumentItemFactory<X13Document, ContextualIds<X13Document>> {

        public MainHighChart() {

            super(X13Document.class, SaViews.MAIN_CHARTS_HIGH, s -> {
                if (s.getResult() == null) {
                    return null;
                }
                int p = s.getInput().getData().getAnnualFrequency();
                int nf = s.getSpecification().getX11().getForecastHorizon();
                if (nf < 0) {
                    nf = -nf * p;
                }
                int nb = s.getSpecification().getX11().getBackcastHorizon();
                if (nb < 0) {
                    nb = -nb * p;
                }
                boolean x11 = s.getResult().getPreprocessing() == null;
                return new ContextualIds<>(highSeries(x11, nb, nf), s);
            }, new ContextualChartUI(true));
        }

        @Override
        public int getPosition() {
            return 2200;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 2300)
    public static class MainTable extends ProcDocumentItemFactory<X13Document, ContextualIds<TsDocument>> {

        public MainTable() {
            super(X13Document.class, SaViews.MAIN_TABLE, s -> {
                if (s.getResult() == null || !s.getResult().isValid()) {
                    return null;
                }
                boolean x11 = s.getResult().getPreprocessing() == null;
                return new ContextualIds<>(finalSeries(x11), s);
            }, new ContextualTableUI(true));
        }

        @Override
        public int getPosition() {
            return 2300;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 2400)
    public static class MainSiFactory extends ProcDocumentItemFactory<X13Document, TsData[]> {

        public MainSiFactory() {
            super(X13Document.class, SaViews.MAIN_SI, (X13Document source) -> {
                X13Results result = source.getResult();
                if (result == null || !result.isValid()) {
                    return null;
                }
                X11Results x11 = result.getDecomposition();
                TsDomain dom = x11.getActualDomain();
                return new TsData[]{
                    TsData.fitToDomain(x11.getD10(), dom),
                    TsData.fitToDomain(x11.getD8(), dom)
                };
            }, new SiRatioUI());
        }

        @Override
        public int getPosition() {
            return 2400;
        }
    }
    //</editor-fold>

//<editor-fold defaultstate="collapsed" desc="PREPROCESSING">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3000)
    public static class SummaryFactory extends ProcDocumentItemFactory<X13Document, HtmlElement> {

        public SummaryFactory() {

            super(X13Document.class, SaViews.PREPROCESSING_SUMMARY, MODELEXTRACTOR
                    .andThen(regarima -> regarima == null ? null
                    : new HtmlRegSarima(regarima, false)),
                    new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 3000;
        }
    }

//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="PREPROCESSING-FORECASTS">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3110)
    public static class ForecastsTable extends ProcDocumentItemFactory<X13Document, ContextualIds<TsDocument>> {

        public ForecastsTable() {
            super(X13Document.class, SaViews.PREPROCESSING_FCASTS_TABLE, s -> {
                if (s.getResult() == null) {
                    return null;
                }
                int nf = s.getSpecification().getX11().getForecastHorizon();
                if (nf < 0) {
                    int p = s.getInput().getData().getAnnualFrequency();
                    nf = -nf * p;
                }
                return new ContextualIds<>(generateItems(nf), s);
            }, new ContextualTableUI(true));
        }

        @Override
        public int getPosition() {
            return 3110;
        }

        private static String[] generateItems(int nf) {
            StringBuilder builder = new StringBuilder();
            builder.append(RegressionDictionaries.Y_F).append('(').append(nf).append(')');
            StringBuilder ebuilder = new StringBuilder();
            ebuilder.append(RegressionDictionaries.Y_EF).append('(').append(nf).append(')');
            return new String[]{builder.toString(), ebuilder.toString()};
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3100)
    public static class FCastsFactory extends ForecastsFactory<X13Document> {

        public FCastsFactory() {
            super(X13Document.class, SaViews.PREPROCESSING_FCASTS, MODELEXTRACTOR);
        }

        @Override
        public int getPosition() {
            return 3100;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3120)
    public static class FCastsOutFactory extends OutOfSampleTestFactory<X13Document> {

        public FCastsOutFactory() {
            super(X13Document.class, SaViews.PREPROCESSING_FCASTS_OUTOFSAMPLE, MODELEXTRACTOR);
        }

        @Override
        public int getPosition() {
            return 3120;
        }
    }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="PREPROCESSING-DETAILS">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3200)
    public static class ModelRegsFactory extends ModelRegressorsFactory<X13Document> {

        public ModelRegsFactory() {
            super(X13Document.class, SaViews.PREPROCESSING_REGS, MODELEXTRACTOR);
        }

        @Override
        public int getPosition() {
            return 3200;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3300)
    public static class ArimaFactory extends ModelArimaFactory {

        public ArimaFactory() {
            super(X13Document.class, SaViews.PREPROCESSING_ARIMA, MODELEXTRACTOR);
        }

        @Override
        public int getPosition() {
            return 3300;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3400)
    public static class PreprocessingDetFactory extends ProcDocumentItemFactory<X13Document, TsDocument> {

        public PreprocessingDetFactory() {
            super(X13Document.class, SaViews.PREPROCESSING_DET, source
                    -> source.getResult().getPreprocessing() == null ? null : source,
                    new GenericTableUI(false,
                            generateId(RegressionDictionaries.YC),
                            generateId(RegressionDictionaries.YLIN),
                            generateId(RegressionDictionaries.YCAL),
                            generateId(RegressionDictionaries.DET),
                            generateId(RegressionDictionaries.CAL),
                            generateId(RegressionDictionaries.TDE),
                            generateId(RegressionDictionaries.EE),
                            generateId(SaDictionaries.OUT_T),
                            generateId(SaDictionaries.OUT_S),
                            generateId(SaDictionaries.OUT_I),
                            generateId(RegressionDictionaries.OUT),
                            generateId(SaDictionaries.REG_Y),
                            generateId(SaDictionaries.REG_SA),
                            generateId(SaDictionaries.REG_T),
                            generateId(SaDictionaries.REG_S),
                            generateId(SaDictionaries.REG_I),
                            generateId(RegressionDictionaries.REG)));
        }

        @Override
        public int getPosition() {
            return 3400;
        }
    }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="PREPROCESSING-RESIDUALS">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3500)
    public static class ModelResFactory extends ProcDocumentItemFactory<X13Document, TsData> {

        public ModelResFactory() {
            super(X13Document.class, SaViews.PREPROCESSING_RES, RESEXTRACTOR,
                    new ResidualsUI()
            );
        }

        @Override
        public int getPosition() {
            return 3500;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3510)
    public static class ModelResStatsFactory extends NiidTestsFactory<X13Document> {

        public ModelResStatsFactory() {
            super(X13Document.class, SaViews.PREPROCESSING_RES_STATS, MODELEXTRACTOR);
        }

        @Override
        public int getPosition() {
            return 3510;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3520)
    public static class ModelResDist extends ProcDocumentItemFactory<X13Document, TsData> {

        public ModelResDist() {
            super(X13Document.class, SaViews.PREPROCESSING_RES_DIST,
                    RESEXTRACTOR,
                    new ResidualsDistUI());

        }

        @Override
        public int getPosition() {
            return 3520;
        }
    }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="PREPROCESSING-OTHERS">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3600)
    public static class LFactory extends LikelihoodFactory<X13Document> {

        public LFactory() {
            super(X13Document.class, SaViews.PREPROCESSING_LIKELIHOOD, MODELEXTRACTOR);
            setAsync(true);
        }

        @Override
        public int getPosition() {
            return 3600;
        }
    }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="REGISTER X11">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4010)
    public static class ATablesFactory extends ProcDocumentItemFactory<X13Document, TsDocument> {

        public ATablesFactory() {
            super(X13Document.class, A_TABLES, source -> source, new GenericTableUI(false,
                    BasicInformationExtractor.prefix(X13Dictionaries.A_TABLE, X13Dictionaries.PREADJUST)));
        }

        @Override
        public int getPosition() {
            return 4010;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4020)
    public static class BTablesFactory extends ProcDocumentItemFactory<X13Document, TsDocument> {

        public BTablesFactory() {
            super(X13Document.class, B_TABLES, source -> source, new GenericTableUI(false,
                    BasicInformationExtractor.prefix(X11Dictionaries.B_TABLE, X13Dictionaries.X11)));
        }

        @Override
        public int getPosition() {
            return 4020;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4030)
    public static class CTablesFactory extends ProcDocumentItemFactory<X13Document, TsDocument> {

        public CTablesFactory() {
            super(X13Document.class, C_TABLES, source -> source, new GenericTableUI(false,
                    BasicInformationExtractor.prefix(X11Dictionaries.C_TABLE, X13Dictionaries.X11)));
        }

        @Override
        public int getPosition() {
            return 4030;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4040)
    public static class DTablesFactory extends ProcDocumentItemFactory<X13Document, TsDocument> {

        static final String[] items = new String[]{
            Dictionary.concatenate(X13Dictionaries.X11, X11Dictionaries.D1),
            Dictionary.concatenate(X13Dictionaries.X11, X11Dictionaries.D4),
            Dictionary.concatenate(X13Dictionaries.X11, X11Dictionaries.D5),
            Dictionary.concatenate(X13Dictionaries.X11, X11Dictionaries.D6),
            Dictionary.concatenate(X13Dictionaries.X11, X11Dictionaries.D7),
            Dictionary.concatenate(X13Dictionaries.X11, X11Dictionaries.D8),
            Dictionary.concatenate(X13Dictionaries.X11, X11Dictionaries.D9),
            Dictionary.concatenate(X13Dictionaries.X11, X11Dictionaries.D10),
            Dictionary.concatenate(X13Dictionaries.X11, X11Dictionaries.D11),
            Dictionary.concatenate(X13Dictionaries.X11, X11Dictionaries.D12),
            Dictionary.concatenate(X13Dictionaries.X11, X11Dictionaries.D13)
        };

        public DTablesFactory() {
            super(X13Document.class, D_TABLES, source -> source, new GenericTableUI(false, items));
        }

        @Override
        public int getPosition() {
            return 4040;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4050)
    public static class DFinalTablesFactory extends ProcDocumentItemFactory<X13Document, X13Document> {

        static final String[] items = new String[]{
            Dictionary.concatenate(X13Dictionaries.FINAL, X13Dictionaries.D11),
            Dictionary.concatenate(X13Dictionaries.FINAL, X13Dictionaries.D11A),
            Dictionary.concatenate(X13Dictionaries.FINAL, X13Dictionaries.D11B),
            Dictionary.concatenate(X13Dictionaries.FINAL, X13Dictionaries.D12),
            Dictionary.concatenate(X13Dictionaries.FINAL, X13Dictionaries.D12A),
            Dictionary.concatenate(X13Dictionaries.FINAL, X13Dictionaries.D12B),
            Dictionary.concatenate(X13Dictionaries.FINAL, X13Dictionaries.D13),
            Dictionary.concatenate(X13Dictionaries.FINAL, X13Dictionaries.D16),
            Dictionary.concatenate(X13Dictionaries.FINAL, X13Dictionaries.D16A),
            Dictionary.concatenate(X13Dictionaries.FINAL, X13Dictionaries.D16B),
            Dictionary.concatenate(X13Dictionaries.FINAL, X13Dictionaries.D18),
            Dictionary.concatenate(X13Dictionaries.FINAL, X13Dictionaries.D18A),
            Dictionary.concatenate(X13Dictionaries.FINAL, X13Dictionaries.D18B)
        };

        public DFinalTablesFactory() {
            super(X13Document.class, D_FINAL_TABLES, VALIDEXTRACTOR, new GenericTableUI(false, items));
        }

        @Override
        public int getPosition() {
            return 4050;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4060)
    public static class ETablesFactory extends ProcDocumentItemFactory<X13Document, X13Document> {

        public ETablesFactory() {
            super(X13Document.class, E_TABLES, VALIDEXTRACTOR, new GenericTableUI(false,
                    BasicInformationExtractor.prefix(X13Dictionaries.E_TABLE, X13Dictionaries.FINAL)));
        }

        @Override
        public int getPosition() {
            return 4060;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4070)
    public static class X11FiltersFactory extends ProcDocumentItemFactory<X13Document, HtmlElement> {

        private static SeasonalFilterOption simplify(SeasonalFilterOption[] option) {
            SeasonalFilterOption opt = option[0];
            for (int i = 1; i < option.length; ++i) {
                if (opt != option[i]) {
                    return null;
                }
            }
            return opt;
        }

        public X11FiltersFactory() {
            super(X13Document.class, X11_FILTERS, (X13Document doc) -> {
                X13Results rslt = doc.getResult();
                if (rslt == null) {
                    return null;
                }
                if (rslt.getDecomposition() == null) {
                    return null;
                }
                return new AbstractHtmlElement() {
                    @Override
                    public void write(HtmlStream stream) throws IOException {
                        stream.write(HtmlTag.HEADER1, "Final filters").newLine();
                        //stream.open(HtmlTag.DIV, "title", "Final seasonal filter");
                        SeasonalFilterOption[] sfilters = rslt.getDecomposition().getFinalSeasonalFilter();
                        if (sfilters != null) {
                            SeasonalFilterOption sfilter = simplify(sfilters);
                            if (sfilter != null) {
                                stream.write("Seasonal filter: ").write(sfilter.name()).newLine();
                            } else {
                                stream.write("Composite seasonal filter: ").newLine();
                                for (int i = 0; i < sfilters.length; i++) {
                                    stream.write((i + 1) + "&#09;" + sfilters[i].name()).newLine();
                                }
                            }
                        }
                        stream.newLine();
                        stream.write("Trend filter: ");
                        stream.write(rslt.getDecomposition().getFinalHendersonFilterLength());
                        stream.write(" terms Henderson moving average").newLine();
                    }
                };

            }, new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 4060;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4200)
    public static class MStatisticsSummaryFactory extends ProcDocumentItemFactory<X13Document, HtmlElement> {

        public MStatisticsSummaryFactory() {
            super(X13Document.class, M_STATISTICS_SUMMARY, (X13Document doc) -> {
                X13Results rslt = doc.getResult();
                if (rslt == null) {
                    return null;
                }
                if (rslt.getDiagnostics() == null) {
                    return null;
                }
                return new HtmlMstatistics(rslt.getDiagnostics().getMstatistics());
            }, new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 4200;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4300)
    public static class MStatisticsDetailsFactory extends ProcDocumentItemFactory<X13Document, HtmlElement> {

        public MStatisticsDetailsFactory() {
            super(X13Document.class, M_STATISTICS_DETAILS, (X13Document doc) -> {
                X13Results rslt = doc.getResult();
                if (rslt == null) {
                    return null;
                }
                if (rslt.getDiagnostics() == null) {
                    return null;
                }
                return new HtmlX11Diagnostics(rslt.getDiagnostics().getMstatistics());
            }, new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 4300;
        }
    }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="DIAGNOSTICS">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5000)
    public static class DiagnosticsSummaryFactory extends ProcDocumentItemFactory<X13Document, HtmlElement> {

        public DiagnosticsSummaryFactory() {
            super(X13Document.class, SaViews.DIAGNOSTICS_SUMMARY, (X13Document doc) -> {
                X13Results rslt = doc.getResult();
                if (rslt == null) {
                    return null;
                }
                SaProcessingFactory factory = SaManager.factoryFor(doc.getSpecification());
                List<ProcDiagnostic> diags = new ArrayList<>();
                factory.fillDiagnostics(diags, null, rslt);
                return new HtmlDiagnosticsSummary(diags);
            }, new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 5000;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5010)
    public static class OriginalSeasonalityFactory extends ProcDocumentItemFactory<X13Document, HtmlElement> {

        public OriginalSeasonalityFactory() {
            super(X13Document.class, SaViews.DIAGNOSTICS_OSEASONALITY, (X13Document doc) -> {
                X13Results rslt = doc.getResult();
                if (rslt == null) {
                    return null;
                }
                if (rslt.getPreadjustment() == null) {
                    return null;
                }
                TsData s;

                if (rslt.getPreprocessing() == null) {
                    s = rslt.getPreadjustment().getA1();
                    if (rslt.getDecomposition().getMode().isMultiplicative()) {
                        s = s.log();
                    }
                } else {
                    s = rslt.getPreprocessing().transformedSeries();
                }
                if (s == null) {
                    return null;
                }
                return new HtmlElements(new HtmlHeader(1, "Original [transformed] series", true),
                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(), s.getAnnualFrequency(), 1, true, true), false));

            }, new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 5010;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5020)
    public static class LinSeasonalityFactory extends ProcDocumentItemFactory<X13Document, HtmlElement> {

        public LinSeasonalityFactory() {
            super(X13Document.class, SaViews.DIAGNOSTICS_LSEASONALITY, (X13Document doc) -> {
                X13Results rslt = doc.getResult();
                if (rslt == null || rslt.getPreprocessing() == null) {
                    return null;
                }
                TsData s = rslt.getPreprocessing().linearizedSeries();
                if (s == null) {
                    return null;
                }
                return new HtmlElements(new HtmlHeader(1, "Linearized series", true),
                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(), s.getAnnualFrequency(), 1, true, true), false));

            }, new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 5020;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5030)
    public static class ResSeasonalityFactory extends ProcDocumentItemFactory<X13Document, HtmlElement> {

        public ResSeasonalityFactory() {
            super(X13Document.class, SaViews.DIAGNOSTICS_RSEASONALITY, (X13Document doc) -> {
                X13Results rslt = doc.getResult();
                if (rslt == null || rslt.getPreprocessing() == null) {
                    return null;
                }
                TsData s = rslt.getPreprocessing().fullResiduals();
                if (s == null) {
                    return null;
                }
                return new HtmlElements(new HtmlHeader(1, "Full residuals", true),
                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(), s.getAnnualFrequency(), 0, false, true), true));

            }, new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 5030;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5035)
    public static class DiagnosticsSeasonalityFactory extends ProcDocumentItemFactory<X13Document, HtmlElement> {

        public DiagnosticsSeasonalityFactory() {
            super(X13Document.class, SaViews.DIAGNOSTICS_SEASONALITY, (X13Document doc) -> {
                X13Results rslt = doc.getResult();
                if (rslt == null || !rslt.isValid()) {
                    return null;
                }
                X11Results x11 = rslt.getDecomposition();
                TsData si = TsData.fitToDomain(x11.getD8(), x11.getActualDomain());
                boolean mul = x11.getMode().isMultiplicative();
                return new HtmlCombinedSeasonalityTest(si, mul);

            }, new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 5035;
        }

    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5040)
    public static class SaSeasonalityFactory extends ProcDocumentItemFactory<X13Document, HtmlElement> {

        public SaSeasonalityFactory() {
            super(X13Document.class, SaViews.DIAGNOSTICS_SASEASONALITY, (X13Document doc) -> {
                X13Results rslt = doc.getResult();
                if (rslt == null || !rslt.isValid()) {
                    return null;
                }
                X11Results x11 = rslt.getDecomposition();
                TsData s = TsData.fitToDomain(x11.getD11(), x11.getActualDomain());
                if (x11.getMode().isMultiplicative()) {
                    s = s.log();
                }
                return new HtmlElements(new HtmlHeader(1, "[Linearized] seasonally adjusted series", true),
                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(), s.getAnnualFrequency(), 1, true, true), true));

            }, new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 5040;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5050)
    public static class IrrSeasonalityFactory extends ProcDocumentItemFactory<X13Document, HtmlElement> {

        public IrrSeasonalityFactory() {
            super(X13Document.class, SaViews.DIAGNOSTICS_ISEASONALITY, (X13Document doc) -> {
                X13Results rslt = doc.getResult();
                if (rslt == null || !rslt.isValid()) {
                    return null;
                }
                X11Results x11 = rslt.getDecomposition();
                TsData s = TsData.fitToDomain(x11.getD13(), x11.getActualDomain());
                if (x11.getMode().isMultiplicative()) {
                    s = s.log();
                }
                return new HtmlElements(new HtmlHeader(1, "[Linearized] irregular component", true),
                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(), s.getAnnualFrequency(), 0, false, true), true));

            }, new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 5050;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5060)
    public static class LastResSeasonalityFactory extends ProcDocumentItemFactory<X13Document, HtmlElement> {

        public LastResSeasonalityFactory() {
            super(X13Document.class, SaViews.DIAGNOSTICS_LASTRSEASONALITY, (X13Document doc) -> {
                X13Results rslt = doc.getResult();
                if (rslt == null || rslt.getPreprocessing() == null) {
                    return null;
                }
                TsData s = rslt.getPreprocessing().fullResiduals();
                if (s == null) {
                    return null;
                }
                StringBuilder header = new StringBuilder().append("Full residuals");
                int ny = DemetraSaUI.get().getSeasonalityLength();
                if (ny > 0) {
                    s = s.drop(Math.max(0, s.length() - s.getAnnualFrequency() * ny), 0);
                    header.append(" (last ").append(ny).append(" years)");
                }
                return new HtmlElements(new HtmlHeader(1, header.toString(), true),
                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(), s.getAnnualFrequency(), 0, false, true), true));

            }, new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 5060;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5070)
    public static class LastSaSeasonalityFactory extends ProcDocumentItemFactory<X13Document, HtmlElement> {

        public LastSaSeasonalityFactory() {
            super(X13Document.class, SaViews.DIAGNOSTICS_LASTSASEASONALITY, (X13Document doc) -> {
                X13Results rslt = doc.getResult();
                if (rslt == null || !rslt.isValid()) {
                    return null;
                }
                X11Results x11 = rslt.getDecomposition();
                TsData s = TsData.fitToDomain(x11.getD11(), x11.getActualDomain());
                if (x11.getMode().isMultiplicative()) {
                    s = s.log();
                }
                StringBuilder header = new StringBuilder().append("[Linearized] seasonally adjusted series");
                int ny = DemetraSaUI.get().getSeasonalityLength();
                if (ny > 0) {
                    s = s.drop(Math.max(0, s.length() - s.getAnnualFrequency() * ny - 1), 0);
                    header.append(" (last ").append(ny).append(" years)");
                }
                return new HtmlElements(new HtmlHeader(1, header.toString(), true),
                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(), s.getAnnualFrequency(), 1, true, true), true));

            }, new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 5070;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5080)
    public static class LastIrrSeasonalityFactory extends ProcDocumentItemFactory<X13Document, HtmlElement> {

        public LastIrrSeasonalityFactory() {
            super(X13Document.class, SaViews.DIAGNOSTICS_LASTISEASONALITY, (X13Document doc) -> {
                X13Results rslt = doc.getResult();
                if (rslt == null || !rslt.isValid()) {
                    return null;
                }
                X11Results x11 = rslt.getDecomposition();
                TsData s = TsData.fitToDomain(x11.getD13(), x11.getActualDomain());
                if (x11.getMode().isMultiplicative()) {
                    s = s.log();
                }
                StringBuilder header = new StringBuilder().append("[Linearized] irregular component");
                int ny = DemetraSaUI.get().getSeasonalityLength();
                if (ny > 0) {
                    s = s.drop(Math.max(0, s.length() - s.getAnnualFrequency() * ny), 0);
                    header.append(" (last ").append(ny).append(" years)");
                }
                return new HtmlElements(new HtmlHeader(1, header.toString(), true),
                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(), s.getAnnualFrequency(), 0, false, true), true));

            }, new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 5080;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5310)
    public static class ModelResSpectrum extends ProcDocumentItemFactory<X13Document, SpectrumUI.Information> {

        public ModelResSpectrum() {
            super(X13Document.class, SaViews.DIAGNOSTICS_SPECTRUM_RES,
                    RESEXTRACTOR.andThen(
                            res
                            -> res == null ? null
                                    : SpectrumUI.Information.builder()
                                            .series(res)
                                            .differencingOrder(0)
                                            .log(false)
                                            .mean(true)
                                            .whiteNoise(true)
                                            .build()),
                    new SpectrumUI());

        }

        @Override
        public int getPosition() {
            return 5310;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5320)
    public static class DiagnosticsSpectrumIFactory extends ProcDocumentItemFactory<X13Document, SpectrumUI.Information> {

        public DiagnosticsSpectrumIFactory() {
            super(X13Document.class, SaViews.DIAGNOSTICS_SPECTRUM_I,
                    DECOMPOSITIONEXTRACTOR.andThen(
                            (X11Results x11) -> {
                                if (x11 == null) {
                                    return null;
                                }
                                TsData s = TsData.fitToDomain(x11.getD13(), x11.getActualDomain());

                                return SpectrumUI.Information.builder()
                                        .series(s)
                                        .differencingOrder(0)
                                        .log(x11.getMode() != DecompositionMode.Additive)
                                        .mean(true)
                                        .whiteNoise(false)
                                        .build();
                            }),
                    new SpectrumUI());
        }

        @Override
        public int getPosition() {
            return 5320;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5330)
    public static class DiagnosticsSpectrumSaFactory extends ProcDocumentItemFactory<X13Document, SpectrumUI.Information> {

        public DiagnosticsSpectrumSaFactory() {
            super(X13Document.class, SaViews.DIAGNOSTICS_SPECTRUM_SA,
                    DECOMPOSITIONEXTRACTOR.andThen(
                            (X11Results x11) -> {
                                if (x11 == null) {
                                    return null;
                                }
                                TsData s = TsData.fitToDomain(x11.getD11(), x11.getActualDomain());

                                return SpectrumUI.Information.builder()
                                        .series(s)
                                        .differencingOrder(1)
                                        .differencingLag(1)
                                        .log(x11.getMode() != DecompositionMode.Additive)
                                        .mean(true)
                                        .whiteNoise(false)
                                        .build();
                            }),
                    new SpectrumUI());
        }

        @Override
        public int getPosition() {
            return 5330;
        }
    }

//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="BENCHMARKING">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4900)
    public static class BenchmarkingFactory extends ProcDocumentItemFactory<X13Document, BenchmarkingUI.Input> {

        public BenchmarkingFactory() {
            super(X13Document.class, SaViews.BENCHMARKING_SUMMARY, (X13Document doc) -> {
                X13Results rslt = doc.getResult();
                if (rslt == null) {
                    return null;
                }
                SaBenchmarkingResults benchmarking = rslt.getBenchmarking();
                if (benchmarking == null) {
                    return null;
                }
                boolean mul = rslt.getDecomposition().getMode().isMultiplicative();
                return new BenchmarkingUI.Input(mul, benchmarking);
            }, new BenchmarkingUI());
        }

        @Override
        public int getPosition() {
            return 4900;
        }

    }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="REGISTER SLIDING SPANS">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 6300)
    public static class DiagnosticsSlidingSummaryFactory extends ProcDocumentItemFactory<X13Document, HtmlElement> {

        public DiagnosticsSlidingSummaryFactory() {
            super(X13Document.class, SaViews.DIAGNOSTICS_SLIDING_SUMMARY, (X13Document source) -> {
                X13Results result = source.getResult();
                if (result == null || !result.isValid()) {
                    return null;
                }
                TsData input = source.getInput().getData();
                TsDomain domain = input.getDomain();
                X13Spec pspec = X13Factory.getInstance().generateSpec(source.getSpecification(), result);
                X13Spec nspec = X13Factory.getInstance().refreshSpec(pspec, source.getSpecification(), EstimationPolicyType.FreeParameters, domain);
                X13Kernel kernel = X13Kernel.of(nspec, source.getContext());
                SlidingSpans<X13Results> ss = new SlidingSpans<>(domain, d -> kernel.process(TsData.fitToDomain(input, d), null));
                boolean mul = result.getDecomposition().getMode().isMultiplicative();
                return new HtmlSaSlidingSpanSummary<>(ss, mul, (X13Results cur) -> {
                    if (cur == null) {
                        return null;
                    }
                    return cur.getDecomposition().getD10();
                }, (var cur) -> {
                    if (cur == null) {
                        return null;
                    }
                    return cur.getDecomposition().getD8();
                });
            },
                    new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 6300;
        }
    }

    private static Function<X13Document, SlidingSpansUI.Information<X13Results>> ssExtractor(String name, boolean changes, Function<X13Results, TsData> fn) {
        return (X13Document source) -> {
            X13Results result = source.getResult();
            if (result == null || !result.isValid()) {
                return null;
            }
            TsData input = source.getInput().getData();
            TsDomain domain = input.getDomain();
            X13Spec pspec = X13Factory.getInstance().generateSpec(source.getSpecification(), result);
            X13Spec nspec = X13Factory.getInstance().refreshSpec(pspec, source.getSpecification(), EstimationPolicyType.FreeParameters, domain);
            X13Kernel kernel = X13Kernel.of(nspec, source.getContext());
            SlidingSpans<X13Results> ss = new SlidingSpans<>(domain, d -> kernel.process(TsData.fitToDomain(input, d), null));
            boolean mul = result.getDecomposition().getMode().isMultiplicative();
            Function<X13Results, TsData> extractor = tsrslt -> {
                if (tsrslt == null) {
                    return null;
                }
                return fn.apply(tsrslt);
            };
            DiagnosticInfo info;
            if (changes) {
                info = mul ? DiagnosticInfo.PeriodToPeriodGrowthDifference : DiagnosticInfo.PeriodToPeriodDifference;
            } else {
                info = mul ? DiagnosticInfo.RelativeDifference : DiagnosticInfo.AbsoluteDifference;
            }
            return new SlidingSpansUI.Information<>(mul, ss, info, name, extractor);
        };
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 6310)
    public static class DiagnosticsSlidingSeasFactory extends ProcDocumentItemFactory<X13Document, SlidingSpansUI.Information<X13Results>> {

        public DiagnosticsSlidingSeasFactory() {
            super(X13Document.class, SaViews.DIAGNOSTICS_SLIDING_SEAS,
                    ssExtractor("Seasonal", false,
                            rslt -> rslt.getDecomposition() == null ? null : rslt.getDecomposition().getD10()),
                    new SlidingSpansUI<X13Results>());
        }

        @Override
        public int getPosition() {
            return 6310;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 6320)
    public static class DiagnosticsSlidingTdFactory extends ProcDocumentItemFactory<X13Document, SlidingSpansUI.Information<X13Results>> {

        public DiagnosticsSlidingTdFactory() {
            super(X13Document.class, SaViews.DIAGNOSTICS_SLIDING_TD,
                    ssExtractor("Trading days", false,
                            rslt -> rslt.getPreprocessing() == null ? null : rslt.getPreprocessing().getTradingDaysEffect(null)),
                    new SlidingSpansUI<X13Results>());
        }

        @Override
        public int getPosition() {
            return 6320;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 6330)
    public static class DiagnosticsSlidingSaFactory extends ProcDocumentItemFactory<X13Document, SlidingSpansUI.Information<X13Results>> {

        public DiagnosticsSlidingSaFactory() {
            super(X13Document.class, SaViews.DIAGNOSTICS_SLIDING_SA,
                    ssExtractor("Seasonally adjusted", true,
                            rslt -> rslt.getFinals().getD11final()),
                    new SlidingSpansUI<X13Results>());
        }

        @Override
        public int getPosition() {
            return 6330;
        }
    }

//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="REGISTER REVISION HISTORY VIEW">
    private static Function<X13Document, RevisionHistoryUI.Information> revisionExtractor(String info, DiagnosticInfo diag) {
        return (X13Document source) -> {
            X13Results result = source.getResult();
            if (result == null || !result.isValid()) {
                return null;
            }
            TsData input = source.getInput().getData();
            TimeSelector span = source.getSpecification().getRegArima().getBasic().getSpan();
            TsDomain domain = input.getDomain().select(span);
            X13Spec pspec = X13Factory.getInstance().generateSpec(source.getSpecification(), result);
            X13Spec nspec = X13Factory.getInstance().refreshSpec(pspec, source.getSpecification(), DemetraSaUI.get().getEstimationPolicyType(), domain);
            if (!span.isAll()) {
                BasicSpec nbasic = nspec.getRegArima().getBasic().toBuilder()
                        .span(TimeSelector.all())
                        .build();
                RegArimaSpec reg = nspec.getRegArima().toBuilder()
                        .basic(nbasic)
                        .build();
                nspec = nspec.toBuilder().regArima(reg).build();
            }
            X13Kernel kernel = X13Kernel.of(nspec, source.getContext());
            RevisionHistory<Explorable> rh = new RevisionHistory<>(domain, d -> kernel.process(TsData.fitToDomain(input, d), null));
            return new RevisionHistoryUI.Information(info, diag, rh);
        };
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 6410)
    public static class RevisionHistorySaFactory extends ProcDocumentItemFactory<X13Document, RevisionHistoryUI.Information> {

        public RevisionHistorySaFactory() {
            super(X13Document.class, SaViews.DIAGNOSTICS_REVISION_SA, revisionExtractor("sa", DiagnosticInfo.RelativeDifference), new RevisionHistoryUI());
        }

        @Override
        public int getPosition() {
            return 6410;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 6420)
    public static class RevisionHistoryTrendFactory extends ProcDocumentItemFactory<X13Document, RevisionHistoryUI.Information> {

        public RevisionHistoryTrendFactory() {
            super(X13Document.class, SaViews.DIAGNOSTICS_REVISION_TREND, revisionExtractor("t", DiagnosticInfo.RelativeDifference), new RevisionHistoryUI());
        }

        @Override
        public int getPosition() {
            return 6420;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 6430)
    public static class RevisionHistorySaChangesFactory extends ProcDocumentItemFactory<X13Document, RevisionHistoryUI.Information> {

        public RevisionHistorySaChangesFactory() {
            super(X13Document.class, SaViews.DIAGNOSTICS_REVISION_SA_CHANGES, revisionExtractor("sa", DiagnosticInfo.PeriodToPeriodGrowthDifference), new RevisionHistoryUI());
        }

        @Override
        public int getPosition() {
            return 6430;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 6440)
    public static class RevisionHistoryTrendChangesFactory extends ProcDocumentItemFactory<X13Document, RevisionHistoryUI.Information> {

        public RevisionHistoryTrendChangesFactory() {
            super(X13Document.class, SaViews.DIAGNOSTICS_REVISION_TREND_CHANGES, revisionExtractor("t", DiagnosticInfo.PeriodToPeriodGrowthDifference), new RevisionHistoryUI());
        }

        @Override
        public int getPosition() {
            return 6440;
        }
    }
    //</editor-fold>

//<editor-fold defaultstate="collapsed" desc="REGISTER STABILITY VIEWS">
    private static Function<X13Document, StabilityUI.Information> stabilityExtractor(EstimationPolicyType policy, String[] items, String msg) {
        return (X13Document source) -> {
            X13Results result = source.getResult();
            if (result == null || result.getPreprocessing() == null) {
                return null;
            }
            TsData input = source.getInput().getData();
            TsDomain domain = input.getDomain();
            RegArimaSpec pspec = RegArimaFactory.getInstance().generateSpec(source.getSpecification().getRegArima(), result.getPreprocessing().getDescription());
            RegArimaSpec nspec = RegArimaFactory.getInstance().refreshSpec(pspec, source.getSpecification().getRegArima(), policy, domain);
            RegArimaKernel kernel = RegArimaKernel.of(nspec, source.getContext());
            MovingProcessing<Explorable> mp = new MovingProcessing<>(domain, (TsDomain d) -> kernel.process(TsData.fitToDomain(input, d), null));
            mp.setWindowLength(DemetraSaUI.get().getStabilityLength() * input.getAnnualFrequency());
            return new StabilityUI.Information(mp, items, msg);
        };
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 6510)
    public static class StabilityTDFactory extends ProcDocumentItemFactory<X13Document, StabilityUI.Information> {

        public StabilityTDFactory() {
            super(X13Document.class, SaViews.DIAGNOSTICS_STABILITY_TD,
                    stabilityExtractor(DemetraSaUI.get().getEstimationPolicyType(), ITEMS, EXCEPTION), new StabilityUI());
        }

        @Override
        public int getPosition() {
            return 6510;
        }

        private static final String EXCEPTION = "No information available on trading days !";
        private static final String[] ITEMS = new String[]{
            "regression.td(1)",
            "regression.td(2)",
            "regression.td(3)",
            "regression.td(4)",
            "regression.td(5)",
            "regression.td(6)",
            "regression.td(7)"
        };

    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 6520)
    public static class StabilityEasterFactory extends ProcDocumentItemFactory<X13Document, StabilityUI.Information> {

        public StabilityEasterFactory() {
            super(X13Document.class, SaViews.DIAGNOSTICS_STABILITY_EASTER,
                    stabilityExtractor(DemetraSaUI.get().getEstimationPolicyType(), ITEMS, EXCEPTION), new StabilityUI());
        }

        private static final String EXCEPTION = "No information available on Easter effects !";
        private static final String[] ITEMS = new String[]{
            "regression.easter"
        };

        @Override
        public int getPosition() {
            return 6520;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 6530)
    public static class StabilityArimaFactory extends ProcDocumentItemFactory<X13Document, StabilityUI.Information> {

        public StabilityArimaFactory() {
            super(X13Document.class, SaViews.DIAGNOSTICS_STABILITY_ARIMA,
                    stabilityExtractor(EstimationPolicyType.FreeParameters, ITEMS, EXCEPTION), new StabilityUI());
        }

        @Override
        public int getPosition() {
            return 6530;
        }

        private static final String EXCEPTION = "No information available on the ARIMA model !";
        private static final String[] ITEMS = new String[]{
            "arima.phi(1)", "arima.phi(2)", "arima.phi(3)", "arima.phi(4)",
            "arima.theta(1)", "arima.theta(2)", "arima.theta(3)", "arima.theta(4)",
            "arima.bphi(1)", "arima.btheta(1)"
        };

    }
    //</editor-fold>
}
