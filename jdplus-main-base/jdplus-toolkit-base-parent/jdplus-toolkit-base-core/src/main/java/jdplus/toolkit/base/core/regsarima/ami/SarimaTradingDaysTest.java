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
package jdplus.toolkit.base.core.regsarima.ami;

import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.calendars.DayClustering;
import jdplus.toolkit.base.api.timeseries.calendars.GenericTradingDays;
import jdplus.toolkit.base.api.timeseries.regression.GenericTradingDaysVariable;
import jdplus.toolkit.base.core.stats.linearmodel.JointTest;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.modelling.regression.Regression;
import jdplus.toolkit.base.core.regarima.RegArimaEstimation;
import jdplus.toolkit.base.core.regarima.RegArimaModel;
import jdplus.toolkit.base.core.regsarima.RegSarimaComputer;
import jdplus.toolkit.base.core.sarima.SarimaModel;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class SarimaTradingDaysTest {
    
    public StatisticalTest sarimaTest(TsData y, SarimaModel arima, boolean mean){
        try {
            GenericTradingDays gtd = GenericTradingDays.contrasts(DayClustering.TD7);
            GenericTradingDaysVariable td = new GenericTradingDaysVariable(gtd);
            FastMatrix m = Regression.matrix(y.getDomain(), td);
            RegArimaModel<SarimaModel> regarima=RegArimaModel.<SarimaModel>builder()
                    .y(y.getValues())
                    .addX(m)
                    .arima(arima)
                    .meanCorrection(mean)
                    .build();
            RegArimaEstimation<SarimaModel> estimation = RegSarimaComputer.PROCESSOR.process(regarima, null);
            return new JointTest(estimation.getConcentratedLikelihood())
                    .hyperParametersCount(arima.getParametersCount())
                    .blue()
                    .variableSelection(mean ? 1 : 0, 6)
                    .build();
        } catch (Exception err) {
            return null;
        }
    }
}
