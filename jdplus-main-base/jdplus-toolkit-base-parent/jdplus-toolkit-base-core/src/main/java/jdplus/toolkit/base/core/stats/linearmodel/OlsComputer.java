/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.stats.linearmodel;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.eco.EcoException;
import jdplus.toolkit.base.api.math.Constants;
import jdplus.toolkit.base.core.math.linearsystem.QRLeastSquaresSolution;
import jdplus.toolkit.base.core.math.linearsystem.QRLeastSquaresSolver;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.MatrixException;

/**
 *
 * @author palatej
 */
public class OlsComputer implements Ols.Processor {

    @Override
    public LeastSquaresResults compute(LinearModel model) {
        try {
            DoubleSeq y = model.getY();
            if (y.norm2() < Constants.getEpsilon()) {
                return null;
            }
            if (model.getVariablesCount() > 0) {
                FastMatrix x = model.variables();
                QRLeastSquaresSolution solution = QRLeastSquaresSolver.robustLeastSquares(y, x);
                FastMatrix bvar = solution.unscaledCovariance();
                return LeastSquaresResults.builder(y, x)
                        .mean(model.isMeanCorrection())
                        .estimation(solution.getB(), bvar)
                        .ssq(solution.getSsqErr())
                        .residuals(solution.getE())
                        .build();
            } else {
                return LeastSquaresResults.builder(y, null)
                        .mean(model.isMeanCorrection())
                        .ssq(y.ssq())
                        .residuals(y)
                        .build();

            }
        } catch (MatrixException err) {
            throw new EcoException(EcoException.OLS_FAILED);
        }
    }
}
