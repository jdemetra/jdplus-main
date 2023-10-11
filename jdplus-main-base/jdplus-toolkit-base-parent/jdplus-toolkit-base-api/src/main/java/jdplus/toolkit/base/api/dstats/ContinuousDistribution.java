/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.toolkit.base.api.dstats;

import jdplus.toolkit.base.api.stats.ProbabilityType;
import nbbrd.design.Development;

/**
 * Represents a continuous statistical distribution (Normal, LogNormal, X2, ...)
 * The domain is a continuum
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public interface ContinuousDistribution extends Distribution {

    /**
     * Returns the lower or upper tail probability of x
     *
     * @param x The value for which the probability is returned
     * @param pt The type of requested probability: lower or upper tail
     * @return The requested probability (double in [0, 1]).
     * @throws DStatException
     */
    double getProbability(double x, ProbabilityType pt) throws DStatException;

    /**
     * Returns the value x that has probability p for the given distribution and
     * probability type
     *
     * @param p The probability
     * @param pt The probability type
     * @return The value x such that P(X &lt x or X &gt x or X = x) = p
     * @throws DStatException
     */
    double getProbabilityInverse(double p, ProbabilityType pt)
            throws DStatException;

    /**
     * Returns the value of x for the density function describing the
     * distribution
     *
     * @param x The value at which density is to be computed
     * @return density(x).
     * @throws DStatException
     */
    double getDensity(double x) throws DStatException;

    /**
     * Returns the left bound.
     *
     * @return The left bound. Can be Double.NEGATIVE INFINITY
     */
    double getLeftBound();

    /**
     * Returns the probability that the variable belongs to the interval [x,y]
     *
     * @param x Lower bound of the interval
     * @param y Upper bound of the interval
     * @return P(X in [x,y]). Belongs to [0, 1].
     * @throws DStatException
     */
    default double getProbabilityForInterval(double x, double y) throws DStatException {
        double l, u;
        if (Double.isFinite(x) && Double.isFinite(y) && x > y) {
            l = y;
            u = x;
        } else {
            l = x;
            u = y;
        }

        double pu = Double.isFinite(u) ? getProbability(u, ProbabilityType.Lower) : 1;
        double pl = Double.isFinite(l) ? getProbability(l, ProbabilityType.Lower) : 0;

        return pu > pl ? pu - pl : 0;
    }

    /**
     * Returns the right bound.
     *
     * @return The right bound. Can be Double.POSITIVEINFINITY.
     */
    double getRightBound();

    /**
     * Generates a random value from the given distribution
     *
     * @param rng the random number generator used to create the value
     * @return The random number
     * @throws DStatException
     */
    double random(RandomNumberGenerator rng) throws DStatException;

}
