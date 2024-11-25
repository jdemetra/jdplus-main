/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package internal.toolkit.base.core.math.functions.gsl.interpolation;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 * n points define n intervals: [x(0), x(1)[...[x(n-2), x(n-1)[,[x(n-1),
 * x(0)+period[
 *
 * @author Jean Palate
 */
public class PeriodicSpline implements DoubleUnaryOperator {

    final FastMatrix c;
    final double[] xa;
    final double period;

    PeriodicSpline(DoubleSeq x, DoubleSeq y, int order, double period) {
        this.xa = x.toArray();
        this.period = period;

        c = FastMatrix.make(x.length(), order);
        c.column(0).copy(y);
    }

    private double interpolate(double x, int index) {
        int order = c.getColumnsCount();
        if (order <= 1) {
            return c.get(index, 0);
        }
        double delx = x - xa[index], p = delx;

        DoubleSeqCursor.OnMutable cursor = c.row(index).cursor();
        double z = cursor.getAndNext() + p * cursor.getAndNext();
        for (int j = 2; j < order; ++j) {
            p *= delx;
            z += p * cursor.getAndNext();
        }
        return z;
    }

    int size() {
        return xa.length;
    }

    @Override
    public double applyAsDouble(double x) {
        if (x < 0 || x >= period) {
            throw new IllegalArgumentException();
        }
        if (x < xa[0]) {
            return interpolate(x + period, c.getRowsCount() - 1);
        } else if (x > xa[xa.length - 1]) {
            return interpolate(x, c.getRowsCount() - 1);
        } else {
            int pos = Arrays.binarySearch(xa, x);
            if (pos >= 0) {
                return c.get(pos, 0);
            } else {
                return interpolate(x, -pos - 2);
            }
        }
    }

}
