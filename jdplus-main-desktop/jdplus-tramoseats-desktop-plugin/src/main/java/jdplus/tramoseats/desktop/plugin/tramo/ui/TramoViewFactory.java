/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.desktop.plugin.tramo.ui;

import jdplus.toolkit.desktop.plugin.html.core.HtmlInformationSet;
import jdplus.toolkit.desktop.plugin.html.modelling.HtmlRegSarima;
import jdplus.tramoseats.base.core.tramo.TramoDocument;
import jdplus.toolkit.desktop.plugin.modelling.ForecastsFactory;
import jdplus.toolkit.desktop.plugin.modelling.InputFactory;
import jdplus.toolkit.desktop.plugin.modelling.LikelihoodFactory;
import jdplus.toolkit.desktop.plugin.modelling.ModelRegressorsFactory;
import jdplus.toolkit.desktop.plugin.modelling.ModelArimaFactory;
import jdplus.toolkit.desktop.plugin.modelling.NiidTestsFactory;
import jdplus.toolkit.desktop.plugin.modelling.OutOfSampleTestFactory;
import jdplus.toolkit.desktop.plugin.modelling.RegSarimaViews;
import jdplus.toolkit.desktop.plugin.ui.processing.GenericTableUI;
import jdplus.toolkit.desktop.plugin.ui.processing.HtmlItemUI;
import jdplus.toolkit.desktop.plugin.ui.processing.IProcDocumentItemFactory;
import jdplus.toolkit.desktop.plugin.ui.processing.IProcDocumentViewFactory;
import jdplus.toolkit.desktop.plugin.ui.processing.ProcDocumentItemFactory;
import jdplus.toolkit.desktop.plugin.ui.processing.ProcDocumentViewFactory;
import jdplus.toolkit.desktop.plugin.ui.processing.stats.ResidualsDistUI;
import jdplus.toolkit.desktop.plugin.ui.processing.stats.ResidualsUI;
import jdplus.toolkit.desktop.plugin.ui.processing.stats.SpectrumUI;
import jdplus.toolkit.desktop.plugin.html.HtmlElement;
import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDocument;
import jdplus.tramoseats.base.information.TramoSpecMapping;
import jdplus.toolkit.base.api.util.Id;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import jdplus.toolkit.base.api.dictionaries.RegressionDictionaries;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
public class TramoViewFactory extends ProcDocumentViewFactory<TramoDocument> {

    private static final AtomicReference<IProcDocumentViewFactory<TramoDocument>> INSTANCE = new AtomicReference();

    private final static Function<TramoDocument, RegSarimaModel> MODELEXTRACTOR = doc -> doc.getResult();
    private final static Function<TramoDocument, TsData> RESEXTRACTOR = doc -> {
        RegSarimaModel result = doc.getResult();
        return result == null ? null : result.fullResiduals();
    };

    public static IProcDocumentViewFactory<TramoDocument> getDefault() {
        IProcDocumentViewFactory<TramoDocument> fac = INSTANCE.get();
        if (fac == null) {
            fac = new TramoViewFactory();
            INSTANCE.lazySet(fac);
        }
        return fac;
    }

    public static void setDefault(IProcDocumentViewFactory<TramoDocument> factory) {
        INSTANCE.set(factory);
    }

    public TramoViewFactory() {
        registerFromLookup(TramoDocument.class);
    }

    @Override
    public Id getPreferredView() {
        return RegSarimaViews.MODEL_SUMMARY;
    }

//<editor-fold defaultstate="collapsed" desc="REGISTER SPEC">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 100010)
    public static class SpecFactory extends ProcDocumentItemFactory<TramoDocument, HtmlElement> {

        public SpecFactory() {
            super(TramoDocument.class, RegSarimaViews.INPUT_SPEC,
                    (TramoDocument doc) -> {
                        InformationSet info = TramoSpecMapping.write(doc.getSpecification(), doc.getInput().getData().getDomain(), true);
                        return new HtmlInformationSet(info);
                    },
                    new HtmlItemUI()
            );
        }

        @Override
        public int getPosition() {
            return 100010;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 100000)
    public static class Input extends InputFactory<TramoDocument> {

        public Input() {
            super(TramoDocument.class, RegSarimaViews.INPUT_SERIES);
        }

        @Override
        public int getPosition() {
            return 100000;
        }
    }

//</editor-fold>
//
//<editor-fold defaultstate="collapsed" desc="REGISTER SUMMARY">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 100000 + 1000)
    public static class SummaryFactory extends ProcDocumentItemFactory<TramoDocument, HtmlElement> {

        public SummaryFactory() {
            super(TramoDocument.class, RegSarimaViews.MODEL_SUMMARY,
                    source -> new HtmlRegSarima(source.getResult(), false),
                    new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 101000;
        }
    }

//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="REGISTER FORECASTS">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 200000 + 500)
    public static class ForecastsTable extends ProcDocumentItemFactory<TramoDocument, TsDocument> {

        public ForecastsTable() {
            super(TramoDocument.class, RegSarimaViews.MODEL_FCASTS_TABLE, s -> s, new GenericTableUI(false, generateItems()));
        }

        @Override
        public int getPosition() {
            return 200500;
        }

        private static String[] generateItems() {
            return new String[]{RegressionDictionaries.Y_F, RegressionDictionaries.Y_EF};
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 200000 + 1000)
    public static class FCastsFactory extends ForecastsFactory<TramoDocument> {

        public FCastsFactory() {
            super(TramoDocument.class, RegSarimaViews.MODEL_FCASTS, MODELEXTRACTOR);
        }

        @Override
        public int getPosition() {
            return 201000;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 200000 + 2000)
    public static class FCastsOutFactory extends OutOfSampleTestFactory<TramoDocument> {

        public FCastsOutFactory() {
            super(TramoDocument.class, RegSarimaViews.MODEL_FCASTS_OUTOFSAMPLE, MODELEXTRACTOR);
        }

        @Override
        public int getPosition() {
            return 202000;
        }
    }

//</editor-fold>
//
//<editor-fold defaultstate="collapsed" desc="REGISTER MODEL">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 300000 + 1000)
    public static class ModelRegsFactory extends ModelRegressorsFactory<TramoDocument> {

        public ModelRegsFactory() {
            super(TramoDocument.class, RegSarimaViews.MODEL_REGS, MODELEXTRACTOR);
        }

        @Override
        public int getPosition() {
            return 301000;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 300000 + 2000)
    public static class ArimaFactory extends ModelArimaFactory {

        public ArimaFactory() {
            super(TramoDocument.class, RegSarimaViews.MODEL_ARIMA, MODELEXTRACTOR);
        }

        @Override
        public int getPosition() {
            return 302000;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 300000 + 3000)
    public static class PreprocessingDetFactory extends ProcDocumentItemFactory<TramoDocument, TramoDocument> {

        public PreprocessingDetFactory() {
            super(TramoDocument.class, RegSarimaViews.MODEL_DET,
                    source -> source, new GenericTableUI(false,
                            RegressionDictionaries.YLIN, RegressionDictionaries.DET,
                            RegressionDictionaries.CAL, RegressionDictionaries.TDE, RegressionDictionaries.EE,
                            RegressionDictionaries.OUT, RegressionDictionaries.FULL_RES));
        }

        @Override
        public int getPosition() {
            return 303000;
        }
    }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="REGISTER RESIDUALS">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 400000 + 1000)
    public static class ModelResFactory extends ProcDocumentItemFactory<TramoDocument, TsData> {

        public ModelResFactory() {
            super(TramoDocument.class, RegSarimaViews.MODEL_RES, RESEXTRACTOR,
                    new ResidualsUI()
            );
        }

        @Override
        public int getPosition() {
            return 401000;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 400000 + 2000)
    public static class ModelResStatsFactory extends NiidTestsFactory<TramoDocument> {

        public ModelResStatsFactory() {
            super(TramoDocument.class, RegSarimaViews.MODEL_RES_STATS, MODELEXTRACTOR);
        }

        @Override
        public int getPosition() {
            return 402000;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 400000 + 3000)
    public static class ModelResDist extends ProcDocumentItemFactory<TramoDocument, TsData> {

        public ModelResDist() {
            super(TramoDocument.class, RegSarimaViews.MODEL_RES_DIST, RESEXTRACTOR,
                    new ResidualsDistUI());

        }

        @Override
        public int getPosition() {
            return 403000;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 400000 + 4000)
    public static class ModelResSpectrum extends ProcDocumentItemFactory<TramoDocument, SpectrumUI.Information> {

        public ModelResSpectrum() {
            super(TramoDocument.class, RegSarimaViews.MODEL_RES_SPECTRUM, 
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
            return 404000;
        }
    }

//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="REGISTER DETAILS">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 500000)
    public static class LFactory extends LikelihoodFactory<TramoDocument> {

        public LFactory() {
            super(TramoDocument.class, RegSarimaViews.MODEL_LIKELIHOOD, MODELEXTRACTOR);
            setAsync(true);
        }

        @Override
        public int getPosition() {
            return 500000;
        }
    }
//</editor-fold>

}
