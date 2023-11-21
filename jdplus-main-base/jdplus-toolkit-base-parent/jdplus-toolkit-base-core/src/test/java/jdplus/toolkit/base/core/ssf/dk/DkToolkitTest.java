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
package jdplus.toolkit.base.core.ssf.dk;

import jdplus.toolkit.base.api.data.DoubleSeq;
import tck.demetra.data.Data;
import jdplus.toolkit.base.api.ssf.sts.SeasonalModel;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.ssf.composite.CompositeSsf;
import jdplus.toolkit.base.core.ssf.likelihood.DiffuseLikelihood;
import jdplus.toolkit.base.core.ssf.sts.LocalLinearTrend;
import jdplus.toolkit.base.core.ssf.sts.Noise;
import jdplus.toolkit.base.core.ssf.sts.SeasonalComponent;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;
import jdplus.toolkit.base.core.stats.likelihood.DiffuseConcentratedLikelihood;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author palatej
 */
public class DkToolkitTest {

    public DkToolkitTest() {
    }

    @Test
    public void testLikelihood() {
        StateComponent ll = LocalLinearTrend.stateComponent(0.5, 0.1);
        StateComponent s = SeasonalComponent.of(SeasonalModel.HarrisonStevens, 12, 0.2);
        StateComponent n = Noise.of(1.5);
        CompositeSsf ssf = CompositeSsf.builder()
                .add(ll, LocalLinearTrend.defaultLoading())
                .add(s, SeasonalComponent.defaultLoading())
                .add(n, Noise.defaultLoading())
                .build();
        DiffuseLikelihood dll0 = DkToolkit.likelihoodComputer(true, true, true)
                .compute(ssf, new SsfData(Data.PROD));
//        System.out.println(dll0);
        DiffuseConcentratedLikelihood dll1 = DkToolkit.concentratedLikelihoodComputer(true, true, false).compute(ssf, new SsfData(Data.PROD));
//        System.out.println(dll1);
        DiffuseLikelihood dll2 = DkToolkit.likelihoodComputer(true, false, true)
                .compute(ssf, new SsfData(Data.PROD));
//        System.out.println(dll2);
    }

    @Test
    public void testLikelihood2() {
        StateComponent ll = LocalLinearTrend.stateComponent(25 * 0.5, 25 * 0.1);
        StateComponent s = SeasonalComponent.of(SeasonalModel.HarrisonStevens, 12, 25 * 0.2);
        StateComponent n = Noise.of(25 * 1.5);
        CompositeSsf ssf = CompositeSsf.builder()
                .add(ll, LocalLinearTrend.defaultLoading())
                .add(s, SeasonalComponent.defaultLoading())
                .add(n, Noise.defaultLoading())
                .build();
        double[] D = Data.PROD;
        DiffuseLikelihood dll1 = DkToolkit.likelihoodComputer(true, true, true)
                .compute(ssf, new SsfData(DoubleSeq.onMapping(D.length, i -> 5 * D[i])));
//        System.out.println(dll1);
        DiffuseLikelihood dll2 = DkToolkit.likelihoodComputer(true, false, true)
                .compute(ssf, new SsfData(DoubleSeq.onMapping(D.length, i -> 5 * D[i])));
//        System.out.println(dll2);
    }

    @Test
    public void testForecast() {
        StateComponent ll = LocalLinearTrend.stateComponent(0.5, 0.1);
        StateComponent s = SeasonalComponent.of(SeasonalModel.HarrisonStevens, 12, 0.2);
        StateComponent n = Noise.of(1.5);
        CompositeSsf ssf = CompositeSsf.builder()
                .add(ll, LocalLinearTrend.defaultLoading())
                .add(s, SeasonalComponent.defaultLoading())
                .add(n, Noise.defaultLoading())
                .build();
        double[] D = Data.PROD;
        FastMatrix F = DkToolkit.forecast(ssf, new SsfData(D), 60, true);
        ssf = CompositeSsf.builder()
                .add(ll, LocalLinearTrend.defaultLoading())
                .add(s, SeasonalComponent.defaultLoading())
                .measurementError(1.5)
                .build();
        FastMatrix F2 = DkToolkit.forecast(ssf, new SsfData(DoubleSeq.onMapping(D.length, i -> 5 * D[i])), 60, true);
        F2.column(0).div(5);
        F2.column(1).div(25);

        assertTrue(F.column(0).distance(F2.column(0)) < 1e-6);
        assertTrue(F.column(1).distance(F2.column(1)) < 1e-6);
    }

}
