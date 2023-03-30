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
package jdplus.toolkit.base.core.regarima;

import tck.demetra.data.Data;
import jdplus.toolkit.base.core.stats.likelihood.LikelihoodStatistics;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.calendars.DayClustering;
import jdplus.toolkit.base.api.timeseries.calendars.GenericTradingDays;
import jdplus.toolkit.base.api.timeseries.calendars.LengthOfPeriodType;
import jdplus.toolkit.base.api.timeseries.regression.EasterVariable;
import jdplus.toolkit.base.api.timeseries.regression.GenericTradingDaysVariable;
import jdplus.toolkit.base.api.timeseries.regression.ITsVariable;
import jdplus.toolkit.base.api.timeseries.regression.LengthOfPeriod;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import java.util.Arrays;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.modelling.regression.Regression;
import jdplus.toolkit.base.core.regsarima.RegSarimaComputer;
import jdplus.toolkit.base.core.regsarima.regular.ModelDescription;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import org.junit.jupiter.api.Test;

/**
 *
 * @author PALATEJ
 */
public class RegArimaForecastsTest {

    public RegArimaForecastsTest() {
    }

    @Test
    public void testSomeMethod() {
        ModelDescription model = new ModelDescription(Data.TS_PROD, null);
        model.setAirline(true);
        model.setMean(true);
//        model.setLogTransformation(true);
//        model.setPreadjustment(LengthOfPeriodType.LeapYear);
        GenericTradingDaysVariable td = new GenericTradingDaysVariable(GenericTradingDays.contrasts(DayClustering.TD7));
        LengthOfPeriod lp = new LengthOfPeriod(LengthOfPeriodType.LeapYear);
        model.addVariable(Variable.variable("td", td));
        EasterVariable easter = EasterVariable.builder()
                .duration(8)
                .meanCorrection(EasterVariable.Correction.PreComputed)
                .endPosition(-1)
                .build();
        model.addVariable(Variable.variable("lp", lp));
        model.addVariable(Variable.variable("easter", easter));
        RegSarimaModel rslt = RegSarimaModel.of(model, RegSarimaComputer.PROCESSOR.process(model.regarima(), model.mapping()), ProcessingLog.dummy());

        TsDomain xdom = model.getEstimationDomain().extend(0, 24);
        Variable[] variables = rslt.getDescription().getVariables(); // could contain the trend const
        FastMatrix matrix = Regression.matrix(xdom, Arrays.stream(variables).map(v -> v.getCore()).toArray(n -> new ITsVariable[n]));

        LikelihoodStatistics ll = rslt.getEstimation().getStatistics();
        double sig2 = ll.getSsqErr() / (ll.getEffectiveObservationsCount() - ll.getEstimatedParametersCount());
        RegArimaForecasts.Result f = RegArimaForecasts.calcForecast(
                rslt.arima(), rslt.getEstimation().originalY(), matrix, 
                rslt.getEstimation().getCoefficients(),
                rslt.getEstimation().getCoefficientsCovariance(), sig2);
    }

}
