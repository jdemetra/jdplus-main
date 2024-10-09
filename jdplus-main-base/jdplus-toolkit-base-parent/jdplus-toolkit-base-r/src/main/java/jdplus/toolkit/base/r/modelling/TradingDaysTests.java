/*
 * Copyright 2017 National Bank of Belgium
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
package jdplus.toolkit.base.r.modelling;

import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.calendars.DayClustering;
import jdplus.toolkit.base.core.data.analysis.WindowFunction;
import jdplus.toolkit.base.core.modelling.regular.tests.CanovaHansenForTradingDays;
import jdplus.toolkit.base.core.modelling.regular.tests.TimeVaryingEstimator;
import jdplus.toolkit.base.core.modelling.regular.tests.TradingDaysTest;
import jdplus.toolkit.base.core.regsarima.ami.SarimaTradingDaysTest;
import jdplus.toolkit.base.core.sarima.SarimaModel;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class TradingDaysTests {

    public double[] canovaHansen(TsData s, int[] diff, String kernel, int truncation) {
        if (truncation<0)
            truncation=(int)Math.floor(0.75*Math.sqrt(s.length()));
        CanovaHansenForTradingDays ch = CanovaHansenForTradingDays.test(s)
                .differencingLags(diff)
                .windowFunction(WindowFunction.valueOf(kernel))
                .truncationLag(truncation)
                .build();
        double[] test = new double[10];
        for (int i = 0; i < 6; ++i) {
            test[i] = ch.test(i);
        }
        test[6]=ch.testDerived();
        test[7] = ch.testAll();
        StatisticalTest tdTest = ch.tdTest();
        test[8] = tdTest.getValue();
        test[9] = tdTest.getPvalue();
        return test;
    }

    public StatisticalTest fTest(TsData s, String model, int ny) {
        s = s.cleanExtremities();
        int freq = s.getAnnualFrequency();
        TsData slast = s;
        if (ny != 0) {
            slast = s.drop(Math.max(0, s.length() - freq * ny), 0);
        }
        if (model.equalsIgnoreCase("D1")) {
            return TradingDaysTest.olsTest(slast, 1);
        } else if (model.equalsIgnoreCase("DY")) {
            return TradingDaysTest.olsTest(slast, freq);
        } else if (model.equalsIgnoreCase("DYD1")) {
            return TradingDaysTest.olsTest(slast, freq, 1);
        } else if (model.equalsIgnoreCase("WN")) {
            return TradingDaysTest.olsTest(slast, null);
        } else if (model.equalsIgnoreCase("AIRLINE")) {
            SarimaOrders orders = SarimaOrders.airline(freq);
            SarimaModel arima = SarimaModel.builder(orders)
                    .setDefault()
                    .build();
            return SarimaTradingDaysTest.sarimaTest(slast, arima, false);
        } else if (model.equalsIgnoreCase("R011")) {
            SarimaOrders orders = SarimaOrders.m011(freq);
            SarimaModel arima = SarimaModel.builder(orders)
                    .setDefault()
                    .build();
            return SarimaTradingDaysTest.sarimaTest(slast, arima, false);
        } else if (model.equalsIgnoreCase("R100")) {
            SarimaOrders orders = new SarimaOrders(freq);
            orders.setP(1);
            SarimaModel arima = SarimaModel.builder(orders)
                    .setDefault()
                    .build();
            return SarimaTradingDaysTest.sarimaTest(slast, arima, true);
        } else {
            return null;
        }
    }

    public StatisticalTest timeVaryingTradingDaysTest(TsData s, int[] td, boolean onContrasts) {
        DayClustering dc= DayClustering.of(td);
        TimeVaryingEstimator estimator=new TimeVaryingEstimator();
        return estimator.process(s, dc, onContrasts);
    }
}
