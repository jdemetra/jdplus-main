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
package jdplus.toolkit.base.core.dstats;

import jdplus.toolkit.base.api.dstats.BoundaryType;
import jdplus.toolkit.base.api.dstats.DStatException;
import jdplus.toolkit.base.api.dstats.DiscreteDistribution;
import jdplus.toolkit.base.api.dstats.RandomNumberGenerator;
import jdplus.toolkit.base.api.stats.ProbabilityType;
import jdplus.toolkit.base.core.stats.Combinatorics;

/**
 *
 * @author palatej
 */
public class Poisson implements DiscreteDistribution {

    private final double mu, emu;

    public Poisson(double mu) {
        this.mu = mu;
        this.emu = Math.exp(-mu);
    }

    @Override
    public long getLeftBound() {
        return 0;
    }

    @Override
    public long getRightBound() {
        return Long.MAX_VALUE;
    }

    @Override
    public String getDescription() {
        return "Poisson - " + Double.toString(mu);
    }

    @Override
    public double getExpectation() throws DStatException {
        return mu;
    }

    @Override
    public double getProbability(long x) throws DStatException {
        double p;
        double lf = Combinatorics.logFactorial(x);
        p = Math.exp(Math.log(mu) * x - lf - mu);
        return p;
    }

    @Override
    public double getVariance() throws DStatException {
        return mu;
    }

    @Override
    public BoundaryType hasLeftBound() {
        return BoundaryType.Finite;
    }

    @Override
    public BoundaryType hasRightBound() {
        return BoundaryType.None;
    }

    @Override
    public boolean isSymmetrical() {
        return false;
    }

    @Override
    public long random(RandomNumberGenerator rng) throws DStatException {
        int rndPoisson = -1;
        double p = 1;
        do {
            p *= rng.nextDouble();
            ++rndPoisson;
        } while (p > emu);
        return rndPoisson;
    }

}
