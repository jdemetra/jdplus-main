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
package jdplus.toolkit.base.r.modelling;

import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.core.data.DataBlockStorage;
import jdplus.toolkit.base.core.data.interpolation.AverageInterpolator;
import jdplus.toolkit.base.core.math.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.toolkit.base.core.math.functions.ssq.SsqFunctionMinimizer;
import jdplus.toolkit.base.core.sarima.estimation.SarimaMapping;
import jdplus.toolkit.base.core.ssf.arima.SsfArima;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
import jdplus.toolkit.base.core.ssf.dk.SsfFunction;
import jdplus.toolkit.base.core.ssf.dk.SsfFunctionPoint;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class Interpolation {

    public TsData averageInterpolation(TsData input) {
        double[] interpolated = AverageInterpolator.interpolator().interpolate(input.getValues(), null);
        return TsData.ofInternal(input.getStart(), interpolated);
    }

    public TsData airlineInterpolation(TsData input) {
        TsPeriod start = input.getStart();
        SarimaOrders spec = SarimaOrders.airline(start.annualFrequency());
        DoubleSeq values = input.getValues();
        SarimaMapping mapping = SarimaMapping.of(spec);
        SsfData data = new SsfData(values);
        SsfFunction fn = SsfFunction.builder(data,
                mapping,
                s -> SsfArima.ssf(s))
                .useFastAlgorithm(false)
                .useScalingFactor(true)
                .useLog(false)
                .useMaximumLikelihood(true)
                .build();
        // estimate 
        SsqFunctionMinimizer fmin = LevenbergMarquardtMinimizer
                .builder()
                .build();
        fmin.minimize(fn.ssqEvaluate(mapping.getDefaultParameters()));
        SsfFunctionPoint rslt= (SsfFunctionPoint) fmin.getResult();
        ISsf ssf = rslt.getSsf();
        DataBlockStorage fs = DkToolkit.fastSmooth(ssf, data);
        double[] g=input.getValues().toArray();
        for (int i=0; i<g.length; ++i){
            if (Double.isNaN(g[i])){
                g[i]=ssf.loading().ZX(i, fs.block(i));
            }
        }
        return TsData.ofInternal(input.getStart(), g);
    }

}
