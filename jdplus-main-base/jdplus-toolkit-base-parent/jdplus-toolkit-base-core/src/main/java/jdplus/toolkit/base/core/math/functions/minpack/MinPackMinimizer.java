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

package jdplus.toolkit.base.core.math.functions.minpack;

import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import nbbrd.design.Development;
import jdplus.toolkit.base.core.math.functions.ssq.ISsqFunctionPoint;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.math.functions.ssq.SsqFunctionMinimizer;


/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class MinPackMinimizer implements SsqFunctionMinimizer {

    public static class MinPackBuilder implements Builder {

        private double fnPrecision = 1e-7;
        private double paramPrecision = 1e-7;
        private double orthogonalTolerance = 1e-7;
        private int maxIter = 1000;

        private MinPackBuilder() {
        }

        @Override
        public MinPackBuilder functionPrecision(double eps) {
            fnPrecision = eps;
            return this;
        }

        public MinPackBuilder parametersPrecision(double eps) {
            paramPrecision = eps;
            return this;
        }

        public MinPackBuilder orthogonalTolerance(double eps) {
            orthogonalTolerance = eps;
            return this;
        }

        @Override
        public MinPackBuilder maxIter(int niter) {
            maxIter = niter;
            return this;
        }

        @Override
        public MinPackMinimizer build() {
            return new MinPackMinimizer(this);
        }

    }

    public static MinPackBuilder builder() {
        return new MinPackBuilder();
    }
    
    
    private LevenbergMarquardtEstimator m_estimator = new LevenbergMarquardtEstimator();

    private SsqEstimationProblem m_problem;
    
    private MinPackMinimizer(MinPackBuilder builder){
        this.m_estimator.setMaxIter(builder.maxIter);
        this.m_estimator.setCostRelativeTolerance(builder.fnPrecision);
        this.m_estimator.setParametersRelativeTolerance(builder.paramPrecision);
        this.m_estimator.setOrthogonalTolerance(builder.orthogonalTolerance);
    }
    /**
     * 
     * @return
     */
    @Override
    public FastMatrix curvatureAtMinimum() {
        try{
	return m_estimator.curvature(m_problem);
        }
        catch(Exception err){
            return null;
        }
    }
    
    @Override
    public DoubleSeq gradientAtMinimum(){
        return this. m_problem.gradient();
    }
            

    @Override
    public ISsqFunctionPoint getResult() {
	return m_problem.getResult();
    }

     @Override
    public double getObjective() {
	return m_problem.getResult() == null ? Double.NaN : m_problem.getResult().getSsqE();
    }
 
    /**
     *
     * @return
     */
    @Override
    public int getIterationsCount() {
        return m_estimator.getIterCount();
    }

    @Override
    public boolean minimize(ISsqFunctionPoint start) {
	m_problem = new SsqEstimationProblem(start);
	try {
	    m_estimator.estimate(m_problem);
	    return m_estimator.getIterCount() < m_estimator.getMaxIter();
	} catch (RuntimeException err) {
	    return false;
	}
    }

 }
