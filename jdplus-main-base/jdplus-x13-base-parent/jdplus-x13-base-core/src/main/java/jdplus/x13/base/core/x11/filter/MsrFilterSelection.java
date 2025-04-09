/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.base.core.x11.filter;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.sa.base.api.DecompositionMode;
import jdplus.x13.base.api.x11.SeasonalFilterOption;
import jdplus.x13.base.core.x11.X11Context;
import static jdplus.x13.base.core.x11.X11Kernel.table;
import jdplus.x13.base.core.x11.filter.endpoints.FilteredMeanEndPoints;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.linearfilters.SymmetricFilter;
import jdplus.x13.base.api.x11.MsrTable;

/**
 *
 * @author Nina Gonschorreck
 */
public class MsrFilterSelection {

    private static final double[] C = {1.00000, 1.02584, 1.01779, 1.01383,
        1.00000, 3.00000, 1.55291, 1.30095};

    private DoubleSeq seas;
    private DoubleSeq irr;

    private MsrTable msrTable;
    private double msr;
    private int iter;
    private boolean useDefault;

    public MsrTable getMsrTable() {
        return msrTable;
    }

    public double getGlobalMsr() {
        return msr;
    }

    public int getIterCount() {
        return iter;
    }

    public boolean isUsingDefault() {
        return useDefault;
    }

    public SeasonalFilterOption doMSR(DoubleSeq data, X11Context context) {
        SeasonalFilterOption seasFilter = null;
        //0. Remove fore- and backcast
        int nf = context.getForecastHorizon();
        int nb = context.getBackcastHorizon();
        DoubleSeq series = data.drop(nb, nf);

        // 0. complete year
        series = completeYear(series, context);
        useDefault = true;
        iter = 0;
        msr = 0;
        while (series.length() / context.getPeriod() >= 5) {
            ++iter;
            // 1. calc Components
            calcComponents(series, context);
            // 2. calc periodic variations
            msrTable = MsrTable.of(seas, irr, context.getPeriod(), context.getPosition(nb), context.getMode() == DecompositionMode.Multiplicative);
            // 3. calc gmsr
            msr = msrTable.getGlobalMsr();
            // 4. decision
            seasFilter = decideFilter(msr);
            if (seasFilter != null) {
                break;
            }
            // 5. cut year
            series = series.drop(0, context.getPeriod());
//          As we have shortend the series, we must adapt the test on the length (5 instead of 6)
        }
        if (seasFilter == null) {
            seasFilter = SeasonalFilterOption.S3X5;
        } else {
            useDefault = false;
        }
        return seasFilter;
    }

    private DoubleSeq completeYear(DoubleSeq series, X11Context context) {
        //check incomplete year
        int start = context.getPosition(context.getBackcastHorizon()), period = context.getPeriod();
        int ecut = (series.length() + start) % period;
        //        int bcut = start == 0 ? 0 : period - start;
        int bcut = 0; // following the Ladiray's paper and the original fortran code !
        return series.drop(bcut, ecut);
    }

    private void calcComponents(DoubleSeq series, X11Context context) {
        // 0. Remove fore- and backcast

        //TODO
        // 1. estimate series component
        SymmetricFilter filter = X11FilterFactory.makeSymmetricFilter(7);
        FilteredMeanEndPoints f = new FilteredMeanEndPoints(filter);

        double[] x = table(series.length(), Double.NaN);
        DataBlock out = DataBlock.of(x);

        for (int j = 0; j < context.getPeriod(); j++) {
            DataBlock bin = DataBlock.of(series).extract(j, -1, context.getPeriod());
            DataBlock bout = out.extract(j, -1, context.getPeriod());
            f.process(bin, bout);
        }

        seas = out;

        // 2. estimate irregular component
        irr = calcIrregular(context, series, seas);
    }

    protected DoubleSeq calcIrregular(X11Context context, DoubleSeq series, DoubleSeq seas) {
        return context.remove(series, seas);
    }

    private SeasonalFilterOption decideFilter(double msr) {
        // table of msr
        if (msr < 2.5) {
            return SeasonalFilterOption.S3X3;
        } else if (msr >= 2.5 && msr < 3.5) {
            return null;
        } else if (msr >= 3.5 && msr < 5.5) {
            return SeasonalFilterOption.S3X5;
        } else if (msr >= 5.5 && msr < 6.5) {
            return null;
        } else {
            return SeasonalFilterOption.S3X9;
        }
    }
}
