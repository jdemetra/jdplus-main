package jdplus.toolkit.base.core.ssf.arima;

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
import tck.demetra.data.Data;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.ssf.StateStorage;
import jdplus.toolkit.base.core.ssf.akf.AkfToolkit;
import jdplus.toolkit.base.core.ssf.akf.AugmentedFilter;
import jdplus.toolkit.base.core.ssf.akf.AugmentedSmoother;
import jdplus.toolkit.base.core.ssf.akf.DefaultAugmentedFilteringResults;
import jdplus.toolkit.base.core.ssf.akf.QAugmentation;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
import jdplus.toolkit.base.core.ssf.composite.CompositeSsf;
import jdplus.toolkit.base.core.ssf.univariate.DefaultSmoothingResults;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.core.ssf.univariate.ISsfData;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import jdplus.toolkit.base.core.ssf.utility.DynamicsCoherence;
import jdplus.toolkit.base.core.ucarima.ModelDecomposer;
import jdplus.toolkit.base.core.ucarima.SeasonalSelector;
import jdplus.toolkit.base.core.ucarima.TrendCycleSelector;
import jdplus.toolkit.base.core.ucarima.UcarimaModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate
 */
public class SsfUcarimaTest {

    public SsfUcarimaTest() {
    }

    @Test
    public void testDynamics() {
        UcarimaModel ucm = ucmAirline(-.6, -.8);
        ucm = ucm.simplify();
        CompositeSsf ssf = SsfUcarima.of(ucm);
        StateComponent cmp = ssf.asComponent();
        DynamicsCoherence.check(cmp.dynamics(), cmp.dim());
    }

    @Test
    public void testDkSmoother() {
        double[] s = Data.RETAIL_BOOKSTORES.clone();
        for (int i = 3; i < 15; ++i) {
            s[i] = Double.NaN;
        }
        s[120] = Double.NaN;
        UcarimaModel ucm = ucmAirline(-.6, -.8);
        ucm = ucm.simplify();
        CompositeSsf ssf = SsfUcarima.of(ucm);
        SsfData data = new SsfData(s);
        DefaultSmoothingResults sd = AkfToolkit.smooth(ssf, data, true, true, true);
        DefaultSmoothingResults ds = DkToolkit.smooth(ssf, data, true, true);
        int[] pos = ssf.componentsPosition();
        for (int i = 0; i < 3; ++i) {
//            System.out.println(sd.getComponent(pos[i]));
//            System.out.println(ds.getComponentVariance(pos[i]));
            assertTrue(ds.getComponent(pos[i]).distance(sd.getComponent(pos[i])) < 1e-6);
            assertTrue(ds.getComponentVariance(pos[i]).distance(sd.getComponentVariance(pos[i])) < 1e-6);
        }
//       System.out.println(sd.getComponentVariance(0));
    }

    public static UcarimaModel ucmAirline(double th, double bth) {
        SarimaOrders spec = SarimaOrders.airline(12);
        SarimaModel sarima = SarimaModel.builder(spec)
                .theta(1, th)
                .btheta(1, bth)
                .build();

        TrendCycleSelector tsel = new TrendCycleSelector();
        SeasonalSelector ssel = new SeasonalSelector(12);

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel);

        UcarimaModel ucm = decomposer.decompose(sarima);
        ucm = ucm.setVarianceMax(-1, false);
        return ucm;
    }

    public static void main(String[] args) {
        UcarimaModel ucm = ucmAirline(-.6, -.8);
        ucm = ucm.simplify();
        CompositeSsf ssf = SsfUcarima.of(ucm);
        SsfData data = new SsfData(Data.RETAIL_BOOKSTORES);
        long t0 = System.currentTimeMillis();
        DefaultSmoothingResults sd = null;
        for (int i = 0; i < 500; ++i) {
            sd = smooth(ssf, data, true, true, QAugmentation.QType.NORMAL);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        System.out.println(sd.getComponent(0));
        System.out.println(sd.getComponentVariance(0).sqrt());
    }

    public static DefaultSmoothingResults smooth(ISsf ssf, ISsfData data, boolean all, boolean rescaleVariance, QAugmentation.QType type) {
        AugmentedSmoother smoother = new AugmentedSmoother();
        smoother.setCalcVariances(all);
        DefaultSmoothingResults sresults = all ? DefaultSmoothingResults.full()
                : DefaultSmoothingResults.light();
        sresults.prepare(ssf.getStateDim(), 0, data.length());
        DefaultAugmentedFilteringResults fresults = filter(ssf, data, true, type);
        if (smoother.process(ssf, data.length(), fresults, sresults)) {
            if (rescaleVariance) {
                rescaleVariances(sresults, var(data.length(), fresults));
            }
            return sresults;
        } else {
            return null;
        }
    }

    public static DefaultAugmentedFilteringResults filter(ISsf ssf, ISsfData data, boolean all, QAugmentation.QType type) {
        QAugmentation Q = QAugmentation.of(type);
        DefaultAugmentedFilteringResults frslts = all
                ? DefaultAugmentedFilteringResults.full(Q) : DefaultAugmentedFilteringResults.light(Q);
        frslts.prepare(ssf, 0, data.length());
        AugmentedFilter filter = new AugmentedFilter();
        filter.process(ssf, data, frslts);
        return frslts;
    }

    public static double var(int n, DefaultAugmentedFilteringResults frslts) {
        double ssq = frslts.getAugmentation().ssq();
        int nd = frslts.getCollapsingPosition();
        int m = frslts.getAugmentation().getDegreesOfFreedom();
        for (int i = nd; i < n; ++i) {
            double e = frslts.error(i);
            if (Double.isFinite(e)) {
                ++m;
                ssq += e * e / frslts.errorVariance(i);
            }
        }
        return ssq / m;
    }

    public static void rescaleVariances(DefaultSmoothingResults r, double v) {
        for (int i = 0; i < r.size(); ++i) {
            r.P(i).mul(v);
        }
    }

}
