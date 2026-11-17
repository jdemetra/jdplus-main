/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.sa.base.core.extractors;

import static jdplus.toolkit.base.core.regarima.extractors.RegSarimaModelExtractors.NBCAST;
import static jdplus.toolkit.base.core.regarima.extractors.RegSarimaModelExtractors.NFCAST;

import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.SaDictionaries;
import jdplus.sa.base.api.SaVariable;
import jdplus.toolkit.base.api.dictionaries.RegressionDictionaries;
import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.toolkit.base.api.modelling.SeriesInfo;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.regression.ModellingUtility;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(InformationExtractor.class)
public class SaRegarimaExtractor extends InformationMapping<RegSarimaModel> {

    @Override
    public Class getSourceClass() {
        return RegSarimaModel.class;
    }

    private static TsData outlier(RegSarimaModel source, ComponentType type, TsDomain domain) {
        TsData s = source.deterministicEffect(
                domain, v -> ModellingUtility.isOutlier(v) && SaVariable.isRegressionEffect(v, type));
        return source.backTransform(s, false);
    }

    private static TsData det(RegSarimaModel source, ComponentType type, TsDomain domain, boolean lpcorr) {
        TsData s = source.deterministicEffect(domain, v -> SaVariable.isRegressionEffect(v, type));
        return source.backTransform(s, lpcorr);
    }

    private static TsData reg(RegSarimaModel source, ComponentType type, TsDomain domain) {
        TsData s = source.deterministicEffect(
                domain, v -> ModellingUtility.isUser(v) && SaVariable.isRegressionEffect(v, type));
        return source.backTransform(s, false);
    }

    public SaRegarimaExtractor() {
        set(SaDictionaries.OUT_I, TsData.class, source -> outlier(source, ComponentType.Irregular, null));
        setArray(
                SaDictionaries.OUT_I + SeriesInfo.F_SUFFIX,
                NFCAST,
                TsData.class,
                (source, i) -> outlier(source, ComponentType.Irregular, source.forecastDomain(i)));
        setArray(
                SaDictionaries.OUT_I + SeriesInfo.B_SUFFIX,
                NBCAST,
                TsData.class,
                (source, i) -> outlier(source, ComponentType.Irregular, source.backcastDomain(i)));
        set(SaDictionaries.OUT_T, TsData.class, source -> outlier(source, ComponentType.Trend, null));
        setArray(
                SaDictionaries.OUT_T + SeriesInfo.F_SUFFIX,
                NFCAST,
                TsData.class,
                (source, i) -> outlier(source, ComponentType.Trend, source.forecastDomain(i)));
        setArray(
                SaDictionaries.OUT_T + SeriesInfo.B_SUFFIX,
                NBCAST,
                TsData.class,
                (source, i) -> outlier(source, ComponentType.Trend, source.backcastDomain(i)));
        set(SaDictionaries.OUT_S, TsData.class, source -> outlier(source, ComponentType.Seasonal, null));
        setArray(
                SaDictionaries.OUT_S + SeriesInfo.F_SUFFIX,
                NFCAST,
                TsData.class,
                (source, i) -> outlier(source, ComponentType.Seasonal, source.forecastDomain(i)));
        setArray(
                SaDictionaries.OUT_S + SeriesInfo.B_SUFFIX,
                NBCAST,
                TsData.class,
                (source, i) -> outlier(source, ComponentType.Seasonal, source.backcastDomain(i)));

        set(SaDictionaries.REG_I, TsData.class, source -> reg(source, ComponentType.Irregular, null));
        setArray(
                SaDictionaries.REG_I_F,
                NFCAST,
                TsData.class,
                (source, i) -> reg(source, ComponentType.Irregular, source.forecastDomain(i)));
        setArray(
                SaDictionaries.REG_I_B,
                NBCAST,
                TsData.class,
                (source, i) -> reg(source, ComponentType.Irregular, source.backcastDomain(i)));
        set(SaDictionaries.REG_T, TsData.class, source -> reg(source, ComponentType.Trend, null));
        setArray(
                SaDictionaries.REG_T_F,
                NFCAST,
                TsData.class,
                (source, i) -> reg(source, ComponentType.Trend, source.forecastDomain(i)));
        setArray(
                SaDictionaries.REG_T_B,
                NBCAST,
                TsData.class,
                (source, i) -> reg(source, ComponentType.Trend, source.backcastDomain(i)));
        set(SaDictionaries.REG_S, TsData.class, source -> reg(source, ComponentType.Seasonal, null));
        setArray(
                SaDictionaries.REG_S_F,
                NFCAST,
                TsData.class,
                (source, i) -> reg(source, ComponentType.Seasonal, source.forecastDomain(i)));
        setArray(
                SaDictionaries.REG_S_B,
                NBCAST,
                TsData.class,
                (source, i) -> reg(source, ComponentType.Seasonal, source.backcastDomain(i)));
        set(SaDictionaries.REG_CAL, TsData.class, source -> reg(source, ComponentType.CalendarEffect, null));
        setArray(
                SaDictionaries.REG_CAL_F,
                NFCAST,
                TsData.class,
                (source, i) -> reg(source, ComponentType.CalendarEffect, source.forecastDomain(i)));
        setArray(
                SaDictionaries.REG_CAL_B,
                NBCAST,
                TsData.class,
                (source, i) -> reg(source, ComponentType.CalendarEffect, source.backcastDomain(i)));
        set(SaDictionaries.REG_SA, TsData.class, source -> reg(source, ComponentType.SeasonallyAdjusted, null));
        setArray(
                SaDictionaries.REG_SA_F,
                NFCAST,
                TsData.class,
                (source, i) -> reg(source, ComponentType.SeasonallyAdjusted, source.forecastDomain(i)));
        setArray(
                SaDictionaries.REG_SA_B,
                NBCAST,
                TsData.class,
                (source, i) -> reg(source, ComponentType.SeasonallyAdjusted, source.backcastDomain(i)));
        set(SaDictionaries.REG_Y, TsData.class, source -> reg(source, ComponentType.Series, null));
        setArray(
                SaDictionaries.REG_Y_F,
                NFCAST,
                TsData.class,
                (source, i) -> reg(source, ComponentType.Series, source.forecastDomain(i)));
        setArray(
                SaDictionaries.REG_Y_B,
                NBCAST,
                TsData.class,
                (source, i) -> reg(source, ComponentType.Series, source.backcastDomain(i)));
        set(SaDictionaries.REG_U, TsData.class, source -> reg(source, ComponentType.Undefined, null));
        setArray(
                SaDictionaries.REG_U_F,
                NFCAST,
                TsData.class,
                (source, i) -> reg(source, ComponentType.Undefined, source.forecastDomain(i)));
        setArray(
                SaDictionaries.REG_U_B,
                NBCAST,
                TsData.class,
                (source, i) -> reg(source, ComponentType.Undefined, source.backcastDomain(i)));

        set(SaDictionaries.DET_I, TsData.class, source -> det(source, ComponentType.Irregular, null, false));
        setArray(
                SaDictionaries.DET_I_F,
                NFCAST,
                TsData.class,
                (source, i) -> det(source, ComponentType.Irregular, source.forecastDomain(i), false));
        setArray(
                SaDictionaries.DET_I_B,
                NBCAST,
                TsData.class,
                (source, i) -> det(source, ComponentType.Irregular, source.backcastDomain(i), false));
        set(SaDictionaries.DET_T, TsData.class, source -> det(source, ComponentType.Trend, null, false));
        setArray(
                SaDictionaries.DET_T_F,
                NFCAST,
                TsData.class,
                (source, i) -> det(source, ComponentType.Trend, source.forecastDomain(i), false));
        setArray(
                SaDictionaries.DET_T_B,
                NBCAST,
                TsData.class,
                (source, i) -> det(source, ComponentType.Trend, source.backcastDomain(i), false));
        set(SaDictionaries.DET_S, TsData.class, source -> det(source, ComponentType.Seasonal, null, true));
        setArray(
                SaDictionaries.DET_S_F,
                NFCAST,
                TsData.class,
                (source, i) -> det(source, ComponentType.Seasonal, source.forecastDomain(i), true));
        setArray(
                SaDictionaries.DET_S_B,
                NBCAST,
                TsData.class,
                (source, i) -> det(source, ComponentType.Seasonal, source.backcastDomain(i), true));
        set(RegressionDictionaries.YLIN, TsData.class, source -> {
            TsData lin = source.linearizedSeries();
            TsData s = source.deterministicEffect(
                    lin.getDomain(), v -> SaVariable.isRegressionEffect(v, ComponentType.Undefined));
            return source.backTransform(TsData.add(lin, s), false);
        });
        setArray(RegressionDictionaries.YLIN_B, NBCAST, TsData.class, (source, i) -> {
            TsData lf = source.linearizedBackcasts(i);
            TsData s = source.deterministicEffect(
                    lf.getDomain(), v -> SaVariable.isRegressionEffect(v, ComponentType.Undefined));
            return source.backTransform(TsData.add(lf, s), false);
        });
        setArray(RegressionDictionaries.YLIN_F, NFCAST, TsData.class, (source, i) -> {
            TsData lf = source.linearizedForecasts(i);
            TsData s = source.deterministicEffect(
                    lf.getDomain(), v -> SaVariable.isRegressionEffect(v, ComponentType.Undefined));
            return source.backTransform(TsData.add(lf, s), false);
        });
        set(SaDictionaries.YCAL, TsData.class, source -> {
            TsData y = source.interpolatedSeries(false);
            TsData cal = det(source, ComponentType.CalendarEffect, null, true);
            return source.inv_op(y, cal);
        });
        setArray(SaDictionaries.YCAL_F, NFCAST, TsData.class, (source, i) -> {
            TsDomain fdom = source.forecastDomain(i);
            TsData yf = source.forecasts(i).getForecasts();
            TsData calf = det(source, ComponentType.CalendarEffect, fdom, true);
            return source.inv_op(yf, calf);
        });
        setArray(SaDictionaries.YCAL_B, NBCAST, TsData.class, (source, i) -> {
            TsDomain bdom = source.backcastDomain(i);
            TsData yb = source.backcasts(i).getForecasts();
            TsData calb = det(source, ComponentType.CalendarEffect, bdom, true);
            return source.inv_op(yb, calb);
        });
        // All calendar effects
        set(SaDictionaries.CAL, TsData.class, source -> det(source, ComponentType.CalendarEffect, null, true));
        setArray(
                SaDictionaries.CAL_F,
                NFCAST,
                TsData.class,
                (source, i) -> det(source, ComponentType.CalendarEffect, source.forecastDomain(i), true));
        setArray(
                SaDictionaries.CAL_B,
                NBCAST,
                TsData.class,
                (source, i) -> det(source, ComponentType.CalendarEffect, source.backcastDomain(i), true));
        // Trading days effects
        set(SaDictionaries.TDE, TsData.class, source -> source.getTradingDaysEffect(null));
        setArray(
                SaDictionaries.TDE_F,
                NFCAST,
                TsData.class,
                (source, i) -> source.getTradingDaysEffect(source.forecastDomain(i)));
        setArray(
                SaDictionaries.TDE_B,
                NBCAST,
                TsData.class,
                (source, i) -> source.getTradingDaysEffect(source.backcastDomain(i)));

        // All moving holidays effects
        set(SaDictionaries.MHE, TsData.class, source -> source.getMovingHolidayEffect(null));
        setArray(
                SaDictionaries.MHE_F,
                NFCAST,
                TsData.class,
                (source, i) -> source.getMovingHolidayEffect(source.forecastDomain(i)));
        setArray(
                SaDictionaries.MHE_B,
                NBCAST,
                TsData.class,
                (source, i) -> source.getMovingHolidayEffect(source.backcastDomain(i)));

        // Easter effect
        set(SaDictionaries.EE, TsData.class, source -> source.getEasterEffect(null));
        setArray(
                SaDictionaries.EE_F,
                NFCAST,
                TsData.class,
                (source, i) -> source.getEasterEffect(source.forecastDomain(i)));
        setArray(
                SaDictionaries.EE_B,
                NBCAST,
                TsData.class,
                (source, i) -> source.getEasterEffect(source.backcastDomain(i)));

        // Other moving holidays effects
        set(SaDictionaries.OMHE, TsData.class, source -> source.getOtherMovingHolidayEffect(null));
        setArray(
                SaDictionaries.OMHE_F,
                NFCAST,
                TsData.class,
                (source, i) -> source.getOtherMovingHolidayEffect(source.forecastDomain(i)));
        setArray(
                SaDictionaries.OMHE_B,
                NBCAST,
                TsData.class,
                (source, i) -> source.getOtherMovingHolidayEffect(source.backcastDomain(i)));
    }
}
