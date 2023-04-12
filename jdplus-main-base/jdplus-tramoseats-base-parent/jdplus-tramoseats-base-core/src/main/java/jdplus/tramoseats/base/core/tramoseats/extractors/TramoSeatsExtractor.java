/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.base.core.tramoseats.extractors;

import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.toolkit.base.api.modelling.ComponentInformation;
import jdplus.toolkit.base.api.modelling.ModellingDictionary;
import jdplus.toolkit.base.api.modelling.SeriesInfo;
import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.DecompositionMode;
import jdplus.sa.base.api.SaDictionaries;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import jdplus.sa.base.core.SaBenchmarkingResults;
import jdplus.tramoseats.base.core.seats.SeatsResults;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsDiagnostics;
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

    public TramoSeatsExtractor() {
        set(SaDictionaries.MODE, DecompositionMode.class, source -> source.getFinals().getMode());

        set(SaDictionaries.SEASONAL, Integer.class, source -> {
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
                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.Value));
        set(SaDictionaries.Y + SaDictionaries.BACKCAST, TsData.class, source
                 -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.Backcast));
        set(SaDictionaries.Y + SaDictionaries.FORECAST, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.Forecast));

        set(ModellingDictionary.CAL, TsData.class, source
                -> source.getPreprocessing().getCalendarEffect(source.getDecomposition().getActualDomain()));
        set(ModellingDictionary.CAL + SaDictionaries.BACKCAST, TsData.class, source
                -> source.getPreprocessing().getCalendarEffect(source.getDecomposition().getBackcastDomain()));
        set(ModellingDictionary.CAL + SaDictionaries.FORECAST, TsData.class, source
                -> source.getPreprocessing().getCalendarEffect(source.getDecomposition().getForecastDomain()));
        

        set(SaDictionaries.T, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.Value));
        set(SaDictionaries.T + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.Forecast));
        set(SaDictionaries.T + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.StdevForecast));
        set(SaDictionaries.T + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.Backcast));
        set(SaDictionaries.T + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.StdevBackcast));

        set(SaDictionaries.SA, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
        set(SaDictionaries.SA + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast));
        set(SaDictionaries.SA + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast));
        set(SaDictionaries.SA + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Backcast));
        set(SaDictionaries.SA + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevBackcast));

        set(SaDictionaries.S, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.Value));
        set(SaDictionaries.S + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.Forecast));
        set(SaDictionaries.S + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.StdevForecast));
        set(SaDictionaries.S + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.Backcast));
        set(SaDictionaries.S + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.StdevBackcast));

        set(SaDictionaries.I, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.Value));
        set(SaDictionaries.I + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.Forecast));
        set(SaDictionaries.I + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.StdevForecast));
        set(SaDictionaries.I + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.Backcast));
        set(SaDictionaries.I + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.StdevBackcast));

        delegate(SaDictionaries.DECOMPOSITION, SeatsResults.class, source -> source.getDecomposition());

        delegate(null, RegSarimaModel.class, source -> source.getPreprocessing());

        delegate(null, TramoSeatsDiagnostics.class, source -> source.getDiagnostics());

        delegate(SaDictionaries.BENCHMARKING, SaBenchmarkingResults.class, source -> source.getBenchmarking());
    }

    @Override
    public Class<TramoSeatsResults> getSourceClass() {
        return TramoSeatsResults.class;
    }
}
