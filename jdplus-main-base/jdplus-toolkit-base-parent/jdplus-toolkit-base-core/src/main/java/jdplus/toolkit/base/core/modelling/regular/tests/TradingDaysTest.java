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
package jdplus.toolkit.base.core.modelling.regular.tests;

import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.calendars.DayClustering;
import jdplus.toolkit.base.api.timeseries.calendars.GenericTradingDays;
import jdplus.toolkit.base.api.timeseries.regression.GenericTradingDaysVariable;
import jdplus.toolkit.base.core.stats.linearmodel.LeastSquaresResults;
import jdplus.toolkit.base.core.stats.linearmodel.LinearModel;
import jdplus.toolkit.base.core.stats.linearmodel.Ols;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.modelling.regression.Regression;
import jdplus.toolkit.base.core.regarima.RegArimaEstimation;
import jdplus.toolkit.base.core.regarima.RegArimaModel;
import jdplus.toolkit.base.core.regsarima.RegSarimaComputer;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.core.stats.linearmodel.JointTest;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class TradingDaysTest {

    /**
     * F test on generic trading days regressors (6 contrast variables)
     *
     * The model is
     *
     * dy(t)-dybar ~ dtd + e
     *
     * dy is the series after differencing and dtd are the trading days after
     * differencing
     *
     * @param y Tested time series.
     * @param lags Differencing lags
     * @return F test
     */
    public StatisticalTest olsTest(TsData y, int... lags) {
        try {
            GenericTradingDays gtd = GenericTradingDays.contrasts(DayClustering.TD7);
            GenericTradingDaysVariable td = new GenericTradingDaysVariable(gtd);
            FastMatrix m = Regression.matrix(y.getDomain(), td);
            DoubleSeq dy = y.getValues();
            FastMatrix dm = m;
            if (lags != null) {
                for (int j = 0; j < lags.length; ++j) {
                    int lag = lags[j];
                    if (lag > 0) {
                        FastMatrix mj = dm;
                        int nr = mj.getRowsCount(), nc = mj.getColumnsCount();
                        dm = mj.extract(lag, nr - lag, 0, nc).deepClone();
                        dm.sub(mj.extract(0, nr - lag, 0, nc));
                        dy = dy.delta(lag);
                    }
                }
            }
            dy = dy.plus(-dy.average());
            LinearModel reg = LinearModel.builder()
                    .y(dy)
                    .addX(dm)
                    .build();
            LeastSquaresResults lsr = Ols.compute(reg);
            return lsr.Ftest();
        } catch (Exception err) {
            return null;
        }
    }

    public StatisticalTest maTest(TsData y, boolean seas) {
        try {
            GenericTradingDays gtd = GenericTradingDays.contrasts(DayClustering.TD7);
            GenericTradingDaysVariable td = new GenericTradingDaysVariable(gtd);
            FastMatrix m = Regression.matrix(y.getDomain(), td);
            SarimaOrders orders;
            
            if (seas)
                orders=SarimaOrders.airline(y.getAnnualFrequency());
            else{
                orders= new SarimaOrders(y.getAnnualFrequency());
                orders.setD(1);
                orders.setQ(1);
            }
            RegArimaModel regarima = RegArimaModel.<SarimaModel>builder()
                    .y(y.getValues())
                    .addX(m)
                    .arima(SarimaModel.builder(orders)
                            .setDefault().build())
                    .build();
            RegArimaEstimation rslt = RegSarimaComputer.PROCESSOR.process(regarima, null);
            return new JointTest(rslt.getConcentratedLikelihood()).hyperParametersCount(seas ? 2 : 1).build();
        } catch (Exception err) {
            return null;
        }
    }

}
