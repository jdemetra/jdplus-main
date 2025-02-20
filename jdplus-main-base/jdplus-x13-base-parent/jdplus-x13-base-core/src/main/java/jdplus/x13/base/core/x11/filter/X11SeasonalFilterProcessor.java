/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.base.core.x11.filter;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.data.DataBlock;

/**
 *
 * @author C. Hofer, N.Gonschorreck
 */
public class X11SeasonalFilterProcessor {

    private final IFiltering[] filters;

    public X11SeasonalFilterProcessor(IFiltering[] filters) {
        this.filters = filters;
    }

    /**
     *
     * @param input
     * @param start start period of the input zero based
     *
     * @return
     */
    public DoubleSeq process(DoubleSeq input, int start) {

        double[] x = new double[input.length()];
        int period = filters.length;

        // conditions for short time series
        int ny_all = input.length() / period;
        int nyr_all = input.length() % period == 0 ? ny_all : ny_all + 1;

        DataBlock out = DataBlock.of(x);
        DataBlock in = DataBlock.of(input);

        int index; //= start;
        for (int i = 0; i < period; ++i) {
            index = (start + i) % period;
            DataBlock cin = in.extract(i, -1, period);
            DataBlock cout = out.extract(i, -1, period);

            // conditions for short time series
            int nf = 0;
            if (filters[index] instanceof X11SeasonalFiltersFactory.DefaultFilter defaultFilter) {
                nf = defaultFilter.getSfilter().getUpperBound();
            }
            DataBlock ccout;
            if (ny_all >= 5 && (nf < 8 || nyr_all >= 20)) { // (nf < 8 || nyr_all >= 20) for S3x15
                ccout = filters[index].process(cin);
            } else {
                X11SeasonalFiltersFactory.StableFilter stable = new X11SeasonalFiltersFactory.StableFilter(period);
                ccout = stable.process(cin);
            }
            cout.set(ccout, y -> y);
        }

        return DoubleSeq.of(x);
    }
}
