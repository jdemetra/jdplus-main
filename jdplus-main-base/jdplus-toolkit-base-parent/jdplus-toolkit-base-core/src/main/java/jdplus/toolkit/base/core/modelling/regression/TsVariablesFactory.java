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
package jdplus.toolkit.base.core.modelling.regression;

import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.toolkit.base.api.timeseries.TimeSeriesDomain;
import jdplus.toolkit.base.api.timeseries.TimeSeriesInterval;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.regression.TsVariables;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
class TsVariablesFactory implements RegressionVariableFactory<TsVariables> {

    static final String NOTFOUND = ": data not found", TOOSHORT = ": series too short. Zeroes added";
    static TsVariablesFactory FACTORY = new TsVariablesFactory();

    private TsVariablesFactory() {
    }

    @Override
    public boolean fill(TsVariables var, TsPeriod start, FastMatrix buffer, ProcessingLog log) {
        int nvars = var.dim();
        int n = buffer.getRowsCount();
        for (int i = 0; i < nvars; ++i) {
            TsData v = var.getData(i);
            if (v == null) {
                if (log != null) {
                    log.warning(var.getId(i) + NOTFOUND);
                }
            } else {
                TsDomain curdom = v.getDomain();
                // position of the first data (in m_ts)
                int istart = curdom.getStartPeriod().until(start);
                // position of the last data (excluded)
                int m = curdom.getLength();
                int iend = istart + n;

                // indexes in data //in buffer
                int jstart = 0, jend = n;
                // not enough data at the beginning
                boolean ok = true;
                if (istart < 0) {
                    ok = false;
                    jstart = -istart;
                    istart = 0;
                }
                // not enough data at the end
                //          if (iend > n) {
                if (iend > m) {
                    jend = jend - (iend - m);
                    iend = m;
                    ok = false;
                }
                if (!ok) {
                    if (log != null) {
                        log.warning(var.getId(i) + TOOSHORT);
                    }
                }
                // iend = v.getValues().length();
                if (jstart < jend) {
                    buffer.column(i).range(jstart, jend).copy(v.getValues().range(istart, iend));
                }
            }
        }
        return true;
    }

    @Override
    public <P extends TimeSeriesInterval<?>, D extends TimeSeriesDomain<P>>
            boolean fill(TsVariables var, D domain,
                    FastMatrix buffer, ProcessingLog log
            ) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
