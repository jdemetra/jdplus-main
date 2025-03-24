/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.base.core.tramoseats.extractors;

import java.util.ArrayList;
import java.util.List;
import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.toolkit.base.api.modelling.ComponentInformation;
import jdplus.toolkit.base.api.modelling.SeriesInfo;
import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.DecompositionMode;
import jdplus.sa.base.api.SaDictionaries;
import jdplus.sa.base.api.SaVariable;
import jdplus.sa.base.api.SeriesDecomposition;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import jdplus.sa.base.core.SaBenchmarkingResults;
import jdplus.toolkit.base.api.arima.SarimaSpec;
import jdplus.toolkit.base.api.dictionaries.Dictionary;
import jdplus.toolkit.base.api.dictionaries.RegArimaDictionaries;
import jdplus.toolkit.base.api.dictionaries.RegressionDictionaries;
import jdplus.toolkit.base.api.processing.ProcDiagnostic;
import jdplus.toolkit.base.api.processing.ProcQuality;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import jdplus.toolkit.base.api.util.IntList;
import jdplus.toolkit.base.core.modelling.GeneralLinearModel;
import jdplus.tramoseats.base.core.seats.SeatsResults;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsDiagnostics;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsFactory;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsResults;
import nbbrd.design.Development;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class TramoSeatsExtractor extends InformationMapping<TramoSeatsResults> {

    public static final String FINAL = "";

    private String advancedItem(String key) {
        return Dictionary.concatenate(RegArimaDictionaries.ADVANCED, key);
    }

    private String qualityItem(String key) {
        return Dictionary.concatenate(SaDictionaries.QUALITY, key);
    }
    
    private TsData finalSeries(TramoSeatsResults source, ComponentType type, ComponentInformation info){
        SeriesDecomposition finals = source.getFinals();
        if (finals == null)
            return null;
        else
            return finals.getSeries(type, info);
    }

    public TramoSeatsExtractor() {
        set(SaDictionaries.MODE, DecompositionMode.class, source -> source.getFinals() == null ? null : source.getFinals().getMode());

        set(SaDictionaries.SEASONAL, Integer.class, source -> {
            if (source.getDecomposition() == null)
                return null;
            TsData s = source.getDecomposition().getInitialComponents()
                    .getSeries(ComponentType.Seasonal, ComponentInformation.Value);
            if (s == null) {
                return 0;
            } else {
                return s.getValues().allMatch(x -> x == 0) ? 0 : 1;
            }
        }
        );
        set(SaDictionaries.Y, TsData.class, source
                -> finalSeries(source, ComponentType.Series, ComponentInformation.Value));
        set(SaDictionaries.Y + SaDictionaries.BACKCAST, TsData.class, source
                -> finalSeries(source, ComponentType.Series, ComponentInformation.Backcast));
        set(SaDictionaries.Y + SaDictionaries.FORECAST, TsData.class, source
                -> finalSeries(source, ComponentType.Series, ComponentInformation.Forecast));

//        set(RegressionDictionaries.CAL, TsData.class, source
//                -> source.getPreprocessing() == null ? null : source.getPreprocessing().getCalendarEffect(source.getDecomposition().getActualDomain()));
//        set(RegressionDictionaries.CAL_B, TsData.class, source
//                -> source.getPreprocessing() == null ? null : source.getPreprocessing().getCalendarEffect(source.getDecomposition().getBackcastDomain()));
//        set(RegressionDictionaries.CAL_F, TsData.class, source
//                -> source.getPreprocessing() == null ? null : source.getPreprocessing().getCalendarEffect(source.getDecomposition().getForecastDomain()));

        set(SaDictionaries.T, TsData.class, source
                -> finalSeries(source, ComponentType.Trend, ComponentInformation.Value));
        set(SaDictionaries.T + SeriesInfo.F_SUFFIX, TsData.class, source
                -> finalSeries(source, ComponentType.Trend, ComponentInformation.Forecast));
        set(SaDictionaries.T + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> finalSeries(source, ComponentType.Trend, ComponentInformation.StdevForecast));
        set(SaDictionaries.T + SeriesInfo.B_SUFFIX, TsData.class, source
                -> finalSeries(source, ComponentType.Trend, ComponentInformation.Backcast));
        set(SaDictionaries.T + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> finalSeries(source, ComponentType.Trend, ComponentInformation.StdevBackcast));

        set(SaDictionaries.SA, TsData.class, source
                -> finalSeries(source, ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
        set(SaDictionaries.SA + SeriesInfo.F_SUFFIX, TsData.class, source
                -> finalSeries(source, ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast));
        set(SaDictionaries.SA + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> finalSeries(source, ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast));
        set(SaDictionaries.SA + SeriesInfo.B_SUFFIX, TsData.class, source
                -> finalSeries(source, ComponentType.SeasonallyAdjusted, ComponentInformation.Backcast));
        set(SaDictionaries.SA + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> finalSeries(source, ComponentType.SeasonallyAdjusted, ComponentInformation.StdevBackcast));

        set(SaDictionaries.S, TsData.class, source
                -> finalSeries(source, ComponentType.Seasonal, ComponentInformation.Value));
        set(SaDictionaries.S + SeriesInfo.F_SUFFIX, TsData.class, source
                -> finalSeries(source, ComponentType.Seasonal, ComponentInformation.Forecast));
        set(SaDictionaries.S + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> finalSeries(source, ComponentType.Seasonal, ComponentInformation.StdevForecast));
        set(SaDictionaries.S + SeriesInfo.B_SUFFIX, TsData.class, source
                -> finalSeries(source, ComponentType.Seasonal, ComponentInformation.Backcast));
        set(SaDictionaries.S + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> finalSeries(source, ComponentType.Seasonal, ComponentInformation.StdevBackcast));

        set(SaDictionaries.I, TsData.class, source
                -> finalSeries(source, ComponentType.Irregular, ComponentInformation.Value));
        set(SaDictionaries.I + SeriesInfo.F_SUFFIX, TsData.class, source
                -> finalSeries(source, ComponentType.Irregular, ComponentInformation.Forecast));
        set(SaDictionaries.I + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> finalSeries(source, ComponentType.Irregular, ComponentInformation.StdevForecast));
        set(SaDictionaries.I + SeriesInfo.B_SUFFIX, TsData.class, source
                -> finalSeries(source, ComponentType.Irregular, ComponentInformation.Backcast));
        set(SaDictionaries.I + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> finalSeries(source, ComponentType.Irregular, ComponentInformation.StdevBackcast));
        set(advancedItem(RegressionDictionaries.REGTYPE), int[].class,
                source -> {
                    if (source.getPreprocessing() == null)
                        return null; 
                    GeneralLinearModel.Description<SarimaSpec> desc = source.getPreprocessing().getDescription();
                    Variable[] vars = desc.getVariables();
                    IntList list = new IntList();
                    for (int i = 0; i < vars.length; ++i) {
                        int n = vars[i].freeCoefficientsCount();
                        if (n > 0) {
                            ComponentType regressionEffect = SaVariable.regressionEffect(vars[i]);
                            for (int j = 0; j < n; ++j) {
                                list.add(regressionEffect.toInt());
                            }
                        }
                    }
                    return list.toArray();
                });
        delegate(SaDictionaries.DECOMPOSITION, SeatsResults.class, source -> source.getDecomposition());

        delegate(null, RegSarimaModel.class, source -> source.getPreprocessing());

        delegate(null, TramoSeatsDiagnostics.class, source -> source.getDiagnostics());

        set(qualityItem(SaDictionaries.QUALITY_SUMMARY), String.class, source -> {
            List<ProcDiagnostic> tests = new ArrayList<>();
             List<String> warnings=new ArrayList<>();
           TramoSeatsFactory.getInstance().fillDiagnostics(tests, warnings, source);
            ProcQuality quality = ProcDiagnostic.summary(tests);
            return quality.name();
        });
        
        delegate(SaDictionaries.BENCHMARKING, SaBenchmarkingResults.class, source -> source.getBenchmarking());
    }

    @Override
    public Class<TramoSeatsResults> getSourceClass() {
        return TramoSeatsResults.class;
    }
}
