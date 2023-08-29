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
package jdplus.sa.base.core.movingtd;

import java.util.Arrays;
import java.util.Optional;
import jdplus.sa.base.api.movingtd.MovingWindowSpec;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.math.linearfilters.FilterSpec;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.regression.ModellingUtility;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.linearfilters.Filtering;
import jdplus.toolkit.base.core.math.linearfilters.FiltersToolkit;
import jdplus.toolkit.base.core.math.linearfilters.IFiltering;
import jdplus.toolkit.base.core.math.linearfilters.SymmetricFiltering;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.modelling.regression.Regression;
import jdplus.toolkit.base.core.regarima.RegArimaEstimation;
import jdplus.toolkit.base.core.regarima.RegArimaModel;
import jdplus.toolkit.base.core.regsarima.RegSarimaComputer;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import jdplus.toolkit.base.core.sarima.SarimaModel;

/**
 *
 * @author palatej
 */
public class MovingWindowEstimator {

    private final MovingWindowSpec spec;

    /**
     * linearized series (including mean effect) + estimated trading days
     * effects
     */
    private TsData partialLinearizedSeries;
    /**
     * Final "moving" trading days effect
     */
    private RegSarimaModel model;
    private FastMatrix td, smoothedTdCoefficients, rawTdCoefficients, tdCoefficients;
    private DoubleSeq startTdCoefficients, startTdStde;
    private TsData tdEffect;
    private TsDomain domain;
    private int freq, wlen, ny, nwindows, cbeg, cend;
    private Variable[] variables;

    public MovingWindowEstimator(MovingWindowSpec spec) {
        this.spec = spec;
    }

    public MovingWindowCorrection process(RegSarimaModel model, int nbcasts, int nfcasts) {
        this.model = model;
        try {
            variables = model.getDescription().getVariables();
            Optional<Variable> otd = Arrays.stream(variables)
                    .filter(var -> !var.isPreadjustment() && ModellingUtility.isTradingDays(var)).findFirst();
            domain = model.getDescription().getDomain();
            Variable vtd = otd.orElseThrow();
            td = Regression.matrix(domain, vtd.getCore());
            if (td.isEmpty()) {
                return null;
            }
            int nfree = vtd.freeCoefficientsCount();
            if (td.getColumnsCount() > nfree) {
                FastMatrix mtdc = FastMatrix.make(td.getRowsCount(), nfree);
                Parameter[] c = vtd.getCoefficients();
                for (int i = 0, j = 0; i < td.getColumnsCount(); ++i) {
                    if (c[i].isFree()) {
                        mtdc.column(j++).copy(td.column(i));
                    }
                }
                td = mtdc;
            }
            TsData tdfixed = model.regressionEffect(domain, var -> ModellingUtility.isTradingDays(var));
            TsData ls = model.linearizedSeries();
            partialLinearizedSeries = TsData.add(ls, tdfixed);
            // lin + td
            if (!computeRawCoefficients()) {
                return null;
            }
            smoothCoefficients();
            computeTdEffects();

            return MovingWindowCorrection.builder()
                    .partialLinearizedSeries(partialLinearizedSeries)
                    .rawCoefficients(rawTdCoefficients)
                    .smoothedCoefficients(smoothedTdCoefficients)
                    .tdCoefficients(tdCoefficients)
                    .tdEffect(tdEffect)
                    .build();
        } finally {
            cleanUp();
        }
    }

    /**
     * The raw coefficients are computed on complete years (a questionable
     * option). So, the figures in incomplete years at the beginning and at the
     * end of the series are discarded.
     *
     * @return
     */
    private boolean computeRawCoefficients() {
        // moving window
        freq = domain.getAnnualFrequency();
        wlen = spec.getWindowLength() * freq;

        int beg = domain.getStartPeriod().annualPosition();
        if (beg != 0) {
            cbeg = freq - beg - 1;
        } else {
            cbeg = 0;
        }
        // cbeg is the number of data discarded at the beginning, 
        // cend the number of data discarded at the end
        cend = domain.getEndPeriod().annualPosition();
        // number of full years
        ny = (domain.getLength() - cbeg - cend) / freq;
        // number of moving windows
        nwindows = ny - spec.getWindowLength() + 1;
        if (nwindows <= 1) {
            return false;
        }
        boolean mean = model.isMeanEstimation();
        rawTdCoefficients = FastMatrix.make(nwindows, td.getColumnsCount());
        for (int i = 0, i0 = cbeg, i1 = cbeg + wlen; i < nwindows; ++i) {
            RegArimaModel<SarimaModel> reg = regarima(partialLinearizedSeries.getValues(), td, mean, model.arima(), i0, i1 - i0);
            if (spec.isReestimate()) {
                RegArimaEstimation<SarimaModel> estimation = RegSarimaComputer.PROCESSOR.optimize(reg, null);
                DoubleSeq b = estimation.getConcentratedLikelihood().coefficients();
                rawTdCoefficients.row(i).copy(mean ? b.drop(1, 0) : b);
            } else {
                RegArimaEstimation<SarimaModel> estimation = RegArimaEstimation.of(reg, 0);
                DoubleSeq b = estimation.getConcentratedLikelihood().coefficients();
                rawTdCoefficients.row(i).copy(mean ? b.drop(1, 0) : b);
            }
            i0 += freq;
            i1 += freq;
        }
        return true;

    }

    private void cleanUp() {
        model = null;
        td = null;
        smoothedTdCoefficients = null;
        rawTdCoefficients = null;
        startTdCoefficients = null;
        startTdStde = null;
        partialLinearizedSeries = null;
        tdEffect = null;
        domain = null;
        freq = wlen = ny = cbeg = 0;
        variables = null;

    }

    private RegArimaModel<SarimaModel> regarima(DoubleSeq data, FastMatrix x, boolean mean, SarimaModel arima, int start, int length) {
        return RegArimaModel.<SarimaModel>builder()
                .meanCorrection(mean)
                .y(data.extract(start, length))
                .addX(x.extract(start, length, 0, x.getColumnsCount()))
                .arima(arima)
                .build();
    }

    private void smoothCoefficients() {
        // extends the matrix of coefficients
        IFiltering filter = FiltersToolkit.of(spec.getFilter());
        int r0 = spec.getWindowLength() / 2, r1 = r0;
        if (cbeg > 0) {
            ++r0;
        }
        if (cend > 0) {
            ++r1;
        }
        int lr = rawTdCoefficients.getRowsCount(), lc = rawTdCoefficients.getColumnsCount();
        FastMatrix x = FastMatrix.make(lr + r0 + r1, lc);
        x.extract(r0, lr, 0, lc).copy(rawTdCoefficients);
        DataBlock row0 = x.row(r0), row1 = x.row(r0 + lr - 1);
        for (int i = 0; i < r0; ++i) {
            x.row(i).copy(row0);
        }
        for (int i = r0 + lr; i < x.getRowsCount(); ++i) {
            x.row(i).copy(row1);
        }
        smoothedTdCoefficients = FastMatrix.make(lr + r0 + r1, lc);
        // apply the smoother on each columns
        for (int i = 0; i < lc; ++i) {
            filter.inPlaceProcess(x.column(i), smoothedTdCoefficients.column(i));
        }
    }

    private void computeTdEffects() {
        
        double[] t=new double[domain.getLength()];
        tdCoefficients=FastMatrix.make(t.length, smoothedTdCoefficients.getColumnsCount());
        // incomplete y0
        int row = 0, i = 0;
        if (cbeg > 0) {
            DataBlock crow = smoothedTdCoefficients.row(row++);
            for (; i < cbeg; ++i) {
                tdCoefficients.row(i).copy(crow);
                t[i]=td.row(i).dot(crow);
            }
        }
        int period = domain.getAnnualFrequency();
        for (int j=0; j<ny; ++j){
            DataBlock crow = smoothedTdCoefficients.row(row++);
            for (int k=0; k < period; ++k, ++i) {
                tdCoefficients.row(i).copy(crow);
                t[i]=td.row(i).dot(crow);
            }
        }
        if (cend > 0) {
            DataBlock crow = smoothedTdCoefficients.row(row++);
            for (int k=0; k<=cend; ++k, ++i) {
                tdCoefficients.row(i).copy(crow);
                t[i]=td.row(i).dot(crow);
            }
        }
        tdEffect=TsData.ofInternal(domain.getStartPeriod(), t);
    }

}
