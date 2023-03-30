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
package jdplus.sa.base.core.spi;

import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.sa.base.api.diagnostics.CombinedSeasonalityTest;
import jdplus.sa.base.api.diagnostics.SeasonalityTests;
import jdplus.toolkit.base.api.stats.OneWayAnova;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.sa.base.core.tests.CombinedSeasonality;
import jdplus.sa.base.core.tests.EvolutiveSeasonality;
import jdplus.sa.base.core.tests.FTest;
import jdplus.sa.base.core.tests.Friedman;
import jdplus.sa.base.core.tests.KruskalWallis;
import jdplus.sa.base.core.tests.Qs;
import jdplus.sa.base.core.tests.StableSeasonality;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(SeasonalityTests.Factory.class)
public class SeasonalityTestsFactory implements SeasonalityTests.Factory {

    @Override
    public OneWayAnova stableSeasonalityTest(DoubleSeq x, int period) {
         return StableSeasonality.of(x, period);
    }

    @Override
    public OneWayAnova evolutiveSeasonalityTest(DoubleSeq x, int period, int startPos, double xbar) {
        return EvolutiveSeasonality.of(x, period, startPos, xbar);
    }

    @Override
    public CombinedSeasonalityTest combinedTest(DoubleSeq x, int period, int startPos, double xbar) {
        CombinedSeasonality cs = new CombinedSeasonality(x, period, startPos, xbar);
        return new CombinedSeasonalityTest(cs.getSummary(), cs.getStableSeasonalityTest(), 
                cs.getEvolutiveSeasonalityTest(), 
                cs.getNonParametricTestForStableSeasonality().build());
    }

    @Override
    public StatisticalTest friedmanTest(DoubleSeq data, int period) {
        return new Friedman(data, period)
                .build();

    }

    @Override
    public StatisticalTest kruskalWallisTest(DoubleSeq data, int period) {
        return new KruskalWallis(data, period)
                .build();
    }

    @Override
    public StatisticalTest qsTest(DoubleSeq data, int period, int nlags) {
        return new Qs(data, period)
                .autoCorrelationsCount(nlags)
                .build();
    }

    @Override
    public StatisticalTest fTest(DoubleSeq data, int period, SarimaOrders.Prespecified model) {
        return new FTest(data, period)
                .model(model)
                .build();
    }

}
