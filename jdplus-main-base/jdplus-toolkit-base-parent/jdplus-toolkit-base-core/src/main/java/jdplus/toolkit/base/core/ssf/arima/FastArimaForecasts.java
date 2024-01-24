/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
 */
package jdplus.toolkit.base.core.ssf.arima;

import jdplus.toolkit.base.core.arima.IArimaModel;
import jdplus.toolkit.base.core.data.DataBlock;
import nbbrd.design.Development;
import jdplus.toolkit.base.core.math.polynomials.Polynomial;
import jdplus.toolkit.base.core.arima.estimation.ArimaForecasts;
import jdplus.toolkit.base.api.data.DoubleSeq;
import internal.toolkit.base.core.arima.MaLjungBoxFilter;

/**
 * The class has been moved to the package
 * jdplus.toolkit.base.core.arima.estimation
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Deprecated
public class FastArimaForecasts implements ArimaForecasts {

    private jdplus.toolkit.base.core.arima.estimation.FastArimaForecasts core
            = new jdplus.toolkit.base.core.arima.estimation.FastArimaForecasts();

    /**
     *
     */
    public FastArimaForecasts() {
    }

    @Override
    public boolean prepare(IArimaModel model, boolean bmean) {
        return core.prepare(model, bmean);
    }

    /**
     *
     * @param model
     * @param mu Value of the mean estimated by regression. Will be internally
     * modified by the stationary AR polynomial
     * @return
     */
    @Override
    public boolean prepare(IArimaModel model, double mu) {
        return core.prepare(model, mu);
    }

    /**
     *
     * @param data
     * @param nf
     * @return
     */
    @Override
    public DoubleSeq forecasts(DoubleSeq data, int nf) {
        return core.forecasts(data, nf);
    }

    @Override
    public double getMean() {
        return core.getMean();
    }
}
