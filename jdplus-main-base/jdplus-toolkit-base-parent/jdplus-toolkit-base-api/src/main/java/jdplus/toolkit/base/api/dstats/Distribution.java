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

import nbbrd.design.Development;

/**
 * Represents a statistical distribution
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public interface Distribution {

    /**
     * Threshold for identifying quasi-zero values
     */
    public static final double EPS_P = 1e-15, EPS_X = 1e-9;

    /**
     * Gets the description of the distribution
     *
     * @return Short description of the distribution
     */
    String getDescription();

    /**
     * Returns E(X); the first moment of the distribution
     *
     * @return Expectation. Can be Double.Nan
     * @throws DStatException
     */
    double getExpectation() throws DStatException;

    /**
     * Returns the second moment of the distribution
     *
     * @return The variance of the distribution. Can be Double.Nan
     * @throws DStatException
     */
    double getVariance() throws DStatException;

    /**
     * Indicates whether the distribution is bounded to the left
     *
     * @return The boundary type
     */
    BoundaryType hasLeftBound();

    /**
     * Indicates whether the distribution is bounded to the right
     *
     * @return The boundary type
     */
    BoundaryType hasRightBound();

    /**
     * Indicates whether the distribution is symmetrical around some central
     * value
     *
     * @return True if the distribution is symmetrical, false otherwise
     */
    boolean isSymmetrical();



}
