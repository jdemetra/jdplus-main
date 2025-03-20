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
package jdplus.toolkit.base.core.stats.likelihood;

import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import nbbrd.design.BuilderPattern;
import jdplus.toolkit.base.api.data.DoubleSeq;
import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status=Development.Status.Release)
public interface DiffuseConcentratedLikelihood extends ConcentratedLikelihood {

    public static Builder builder(int n, int nd, int nxd) {
        return new Builder(n, nd, nxd);
    }

    @BuilderPattern(DiffuseConcentratedLikelihood.class)
    public static class Builder {

        private final int n, nd, nxd;
        private double ssqerr, ldet, lddet;
        private double[] res;
        private boolean legacy;
        private double[] b;
        private FastMatrix bvar;
        private boolean scalingFactor = true;

        Builder(int n, int nd, int nxd) {
            this.n = n;
            this.nd = nd;
            this.nxd = nxd;
        }

        public Builder scalingFactor(boolean scalingFactor) {
            this.scalingFactor = scalingFactor;
            return this;
        }

        public Builder logDeterminant(double ldet) {
            this.ldet = ldet;
            return this;
        }

        public Builder logDiffuseDeterminant(double lddet) {
            this.lddet = lddet;
            return this;
        }

        public Builder ssqErr(double ssq) {
            this.ssqerr = ssq;
            return this;
        }

        public Builder legacy(boolean legacy) {
            this.legacy = legacy;
            return this;
        }

        public Builder residuals(DoubleSeq residuals) {
            if (residuals == null) {
                return this;
            }
            if (ssqerr == 0) {
                this.ssqerr = residuals.ssq();
            }
            this.res = residuals.toArray();
            return this;
        }

        public Builder coefficients(DoubleSeq coeff) {
            if (coeff != null) {
                b = coeff.toArray();
            }
            return this;
        }

        public Builder unscaledCovariance(FastMatrix var) {
            bvar = var;
            return this;
        }

        public DiffuseConcentratedLikelihood build() {
            return new InternalDiffuseConcentratedLikelihood(n, nd, nxd, ssqerr, 
                    ldet, lddet, b, bvar, res, legacy, scalingFactor);
        }
    }

    /**
     * Contains diffuse regression variables
     * @return 
     */
    int ndiffuse();
    
    int ndiffuseRegressors();

    double diffuseCorrection();

    /**
     * Adjust the likelihood if the array has been pre-multiplied by a given
     * scaling factor
     *
     * @param yfactor
     * @param xfactor
     * @return
     */
    DiffuseConcentratedLikelihood rescale(final double yfactor, double[] xfactor);

    @Override
    default int degreesOfFreedom(){
        return dim()-(nx()-ndiffuseRegressors())-ndiffuse();
    }
    
    default DiffuseLikelihoodStatistics stats(double llcorrection, int nparams) {
        return DiffuseLikelihoodStatistics.builder()
                .logLikelihood(this.logLikelihood())
                .logDeterminant(this.logDeterminant())
                .diffuseCorrection(this.diffuseCorrection())
                .transformationAdjustment(llcorrection)
                .observationsCount(this.dim())
                .ssqErr(ssq())
                .diffuseCount(ndiffuse())
                .estimatedParametersCount(nparams + (nx()-ndiffuseRegressors()))
                .build();
    }

}
