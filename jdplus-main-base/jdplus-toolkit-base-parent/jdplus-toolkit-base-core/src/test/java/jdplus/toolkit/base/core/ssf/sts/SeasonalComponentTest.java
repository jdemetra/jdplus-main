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
package jdplus.toolkit.base.core.ssf.sts;

import jdplus.toolkit.base.api.ssf.sts.SeasonalModel;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.ssf.utility.DynamicsCoherence;
import jdplus.toolkit.base.core.ssf.utility.LoadingCoherence;
import org.junit.jupiter.api.Test;

/**
 *
 * @author PALATEJ
 */
public class SeasonalComponentTest {

    public SeasonalComponentTest() {
    }

    @Test
    public void testDynamics() {
        for (int p = 2; p <= 12; ++p) {
            StateComponent s = SeasonalComponent.of(SeasonalModel.HarrisonStevens, p, 1);
            DynamicsCoherence.check(s.dynamics(), s.dim());
            s = SeasonalComponent.of(SeasonalModel.Crude, p, 1);
            DynamicsCoherence.check(s.dynamics(), s.dim());
            s = SeasonalComponent.of(SeasonalModel.Trigonometric, p, 1);
            DynamicsCoherence.check(s.dynamics(), s.dim());
            s = SeasonalComponent.of(SeasonalModel.Dummy, p, 1);
            DynamicsCoherence.check(s.dynamics(), s.dim());
        }
    }

    @Test
    public void testMeasurement() {
        ISsfLoading l = CyclicalComponent.defaultLoading();
        LoadingCoherence.check(l, LocalLinearTrend.dim());
    }
}
