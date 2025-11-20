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
package jdplus.toolkit.base.core.regarima.outlier;

import jdplus.toolkit.base.core.stats.RobustStandardDeviationComputer;
import jdplus.toolkit.base.core.arima.IArimaModel;
import jdplus.toolkit.base.core.arima.estimation.ResidualsComputer;
import internal.toolkit.base.core.arima.AnsleyFilter;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.UpperTriangularMatrix;
import nbbrd.design.Development;
import jdplus.toolkit.base.core.math.linearfilters.BackFilter;
import jdplus.toolkit.base.core.regarima.RegArimaModel;
import jdplus.toolkit.base.core.regarima.RegArmaModel;
import jdplus.toolkit.base.core.math.linearsystem.QRLeastSquaresSolver;
import jdplus.toolkit.base.core.arima.estimation.ArmaFilter;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.core.math.linearfilters.RationalBackFilter;
import jdplus.toolkit.base.core.math.linearsystem.QRLeastSquaresSolution;
import jdplus.toolkit.base.core.math.polynomials.Polynomial;
import jdplus.toolkit.base.core.modelling.regression.IOutlierFactory;

/**
 *
 * @author Jean Palate
 * @param <T>
 */
@Development(status = Development.Status.Preliminary)
public class FastOutlierDetector2<T extends IArimaModel> extends SingleOutlierDetector<T> {

    private final ArmaFilter filter;
    private final ResidualsComputer resComputer;
    private FastMatrix U, Xl;
    private int[] pivot;
    private double[] yl, b, z;
    private int n;
    private double mad;

    /**
     *
     * @param computer
     * @param filter
     * @param res
     */
    public FastOutlierDetector2(RobustStandardDeviationComputer computer, ArmaFilter filter, ResidualsComputer res) {
        super(computer == null ? RobustStandardDeviationComputer.mad() : computer);
        this.filter = filter == null ? new AnsleyFilter() : filter;
        resComputer = res == null ? ResidualsComputer.defaultComputer(this.filter) : res;
    }

    /**
     *
     * @return
     */
    @Override
    protected boolean calc() {
        try {
            if (getOutlierFactoriesCount() == 0 || ubound <= lbound) {
                return false;
            }
            RegArmaModel<T> dmodel = this.getRegArima().differencedModel();
            n = filter.prepare(dmodel.getArma(), dmodel.getY().length());
            if (!initialize(dmodel)) {
                return false;
            }
            for (int i = 0; i < getOutlierFactoriesCount(); ++i) {
                processOutlier(i);
            }
            return true;
        } catch (Exception err) {
            return false;
        }
    }

    /**
     *
     * @param model
     * @return
     */
    protected boolean initialize(RegArmaModel<T> model) {
        try {
            //  yl
            yl = new double[n];
            DataBlock Yl = DataBlock.of(yl);
            filter.apply(model.getY(), Yl);

            // Xl
            FastMatrix regs = model.getX();
            if (regs.isEmpty()) {
                mad = getStandardDeviationComputer().compute(filter(model.getY()));
                return true;
            }

            Xl = FastMatrix.make(n, regs.getColumnsCount());
            DataBlockIterator rcols = regs.columnsIterator(), drcols = Xl.columnsIterator();
            while (rcols.hasNext()) {
                filter.apply(rcols.next(), drcols.next());
            }

            QRLeastSquaresSolution ls = QRLeastSquaresSolver.robustLeastSquares(Yl, Xl);
            if (ls.rank() != regs.getColumnsCount()) {
                return false;
            }
            pivot = ls.pivot();
            U = ls.rawR();

            // z = yl'*Xl
            int nx = Xl.getColumnsCount();
            z = new double[nx];
            for (int i = 0; i < nx; ++i) {
                z[i] = Yl.dot(Xl.column(pivot[i]));
            }
            // z = z U^-1
            UpperTriangularMatrix.solvexU(U, DataBlock.of(z));

            DoubleSeq B = ls.getB();
            b = B.toArray();

            // calcMAD(E);
            DataBlock e = DataBlock.of(model.getY());

            // b are in the right order
            DoubleSeqCursor coeff = ls.getB().cursor();
            rcols.begin();
            while (rcols.hasNext()) {
                e.addAY(-coeff.getAndNext(), rcols.next());
            }
            mad = getStandardDeviationComputer().compute((filter(e)));

            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    /**
     *
     * @param idx
     */
    protected void processOutlier(int idx) {
        RegArimaModel<T> regArima = getRegArima();
        int len = regArima.getY().length();
        BackFilter df = regArima.arima().getNonStationaryAr();
        int d = df.getDegree();
        IOutlierFactory.FilterRepresentation representation = getOutlierFactory(idx).getFilterRepresentation();
        if (representation == null) {
            return;
        }
        IArimaModel model = getRegArima().arima();
        RationalBackFilter pi = model.getPiWeights();
        double[] o = pi.times(representation.filter).getWeights(len);
            double corr = 0;
        if (d == 0 && representation.correction != 0) {
            // TODO: apply other corrections based on corr !!
            Polynomial ar = model.getAr().asPolynomial();
            Polynomial ma = model.getMa().asPolynomial();
            corr = representation.correction * ar.evaluateAt(1) / ma.evaluateAt(1);
            for (int i = 0; i < 2*n; ++i) {
                o[i] += corr;
            }
        }

        DataBlock Yl = DataBlock.of(yl);

        int nx = Xl == null ? 0 : Xl.getColumnsCount();

        for (int i = lbound; i < ubound; ++i) {
            if (isAllowed(i, idx)) {
                // ol
                DataBlock Olc;
                FastMatrix Xlc=null;
                DataBlock Ylc;
                if (i < d){
                    Olc=DataBlock.of(o, d-i, d-i+n, 1);
                    Ylc=Yl;
                    if (nx>0){
                        Xlc=Xl;
                    } 
                }else{
                    Olc=DataBlock.of(o, 0, len-i, 1);
                    Ylc=Yl.drop(i-d,0);
                    if (nx>0){
                        Xlc=Xl.extract(i-d, len-i, 0, nx);
                    } 
                }
                double xx = Olc.ssq(), xy = Olc.dot(Ylc);

                if (U != null) {
                    // w=ol*Xl
                    double[] w = new double[b.length];
                    for (int q = 0; q < nx; ++q) {
                        w[q] = Olc.dot(Xlc.column(pivot[q]));
                    }
                    DataBlock a = DataBlock.of(w);
                    // a=wU^-1
                    UpperTriangularMatrix.solvexU(U, a);
                    // q = l'A^{-1}l
                    double q = a.ssq();
                    //
                    double v = xx - q;
                    if (v <= 0) {
                        exclude(i, idx);
                    } else {
                        double c = xy - DataBlock.of(z).dot(a);
                        setT(i, idx, c / (Math.sqrt(v) * mad));
                        setCoefficient(i, idx, c / v);
                    }
                } else if (xx <= 0) {
                    exclude(i, idx);
                    setT(i, idx, Double.NaN);
                    setCoefficient(i, idx, Double.NaN);
                } else {
                    setT(i, idx, xy / (Math.sqrt(xx) * mad));
                    setCoefficient(i, idx, xy / xx);
                }
            } else {
                setT(i, idx, Double.NaN);
                setCoefficient(i, idx, Double.NaN);
            }
        }
    }

    protected DoubleSeq filter(DoubleSeq res) {
        return resComputer.residuals(this.getRegArima().differencedModel().getArma(), res);
    }

    @Override
    protected void clear(boolean all) {
        super.clear(all);
        U = null;
        Xl = null;
        b = null;
        z = null;
    }
}
