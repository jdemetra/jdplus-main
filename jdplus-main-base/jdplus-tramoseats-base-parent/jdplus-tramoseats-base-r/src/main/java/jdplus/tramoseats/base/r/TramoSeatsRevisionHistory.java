/*
 * Copyright 2022 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.tramoseats.base.r;

import java.time.LocalDate;
import java.util.List;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDataTable;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import jdplus.toolkit.base.api.timeseries.regression.RegressionItem;
import jdplus.toolkit.base.core.timeseries.simplets.analysis.DiagnosticInfo;
import jdplus.toolkit.base.core.timeseries.simplets.analysis.RevisionHistory;
import jdplus.tramoseats.base.api.tramoseats.TramoSeatsSpec;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsKernel;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsResults;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class TramoSeatsRevisionHistory {

    @lombok.Value
    public static class Revisions {

        RevisionHistory<TramoSeatsResults> revisions;

        public TsDomain referenceDomain() {
            return revisions.getReferenceDomain();
        }

        public TramoSeatsResults result(String period) {
            LocalDate date = LocalDate.parse(period);
            TsDomain referenceDomain = revisions.getReferenceDomain();
            int id = referenceDomain.indexOf(date.atStartOfDay());
            if (id < 0) {
                return null;
            }
            TsDomain cur = referenceDomain.range(0, id + 1);
            return revisions.tsInfo(cur);
        }

        public TsData history(String id, String start) {
            LocalDate date = LocalDate.parse(start);
            TsDomain referenceDomain = revisions.getReferenceDomain();
            TsPeriod pstart = TsPeriod.of(referenceDomain.getTsUnit(), date);
            return revisions.revision(pstart, rslt -> toDouble(rslt.getData(id, Object.class)));
        }

        public TsData tsHistory(String id, String period, String start) {
            LocalDate date = LocalDate.parse(start), refDate = LocalDate.parse(period);
            TsDomain referenceDomain = revisions.getReferenceDomain();
            TsPeriod pstart = TsPeriod.of(referenceDomain.getTsUnit(), date),
                    pref = TsPeriod.of(referenceDomain.getTsUnit(), refDate);
            return revisions.tsRevision(pref, pstart, rslt -> rslt.getData(id, TsData.class));
        }
        
        public TsDataTable tsSelect(String id, String start, String end) {
            LocalDate d0 = LocalDate.parse(start), d1 = LocalDate.parse(end);
            List<TsData> sel = revisions.select(d0, d1, r->r.getData(id, TsData.class));
            TsDataTable table=TsDataTable.of(sel);
            return table;
        }
        
        public TsDomain referenceDomain(String id){
            TsData data = revisions.getReferenceInfo().getData(id, TsData.class);
            return data == null ? null : data.getDomain();
        }
 
        public TsData diagnosticOf(String id, String start, String end, String diag) {
            LocalDate d0 = LocalDate.parse(start), d1 = LocalDate.parse(end);
            TsUnit unit = revisions.getReferenceDomain().getTsUnit();
            TsPeriod p0 = TsPeriod.of(unit, d0), p1=TsPeriod.of(unit, d1);
            TsDomain domain=TsDomain.of(p0, p0.until(p1)+1);
            if (domain.isEmpty())
                return null;
            DiagnosticInfo info = DiagnosticInfo.valueOf(diag);
            double[] revs = new double[domain.length()];
            for (int i = 0; i < revs.length; ++i) {
                revs[i] = revisions.seriesRevision(p0.plus(i), info, rslt -> rslt.getData(id, TsData.class));
            }
            return TsData.ofInternal(p0, revs);

        }

    }

    public Revisions revisions(TsData series, TramoSeatsSpec spec, ModellingContext context) {
        TramoSeatsKernel kernel = TramoSeatsKernel.of(spec, context);
        RevisionHistory<TramoSeatsResults> rh = new RevisionHistory<>(series.getDomain(), d -> kernel.process(TsData.fitToDomain(series, d), ProcessingLog.dummy()));
        return new Revisions(rh);
    }

    private static double toDouble(Object obj) {
        if (obj == null) {
            return Double.NaN;
        }
        if (obj instanceof Double dobj) {
            return dobj;
        }
        if (obj instanceof Integer iobj) {
            return iobj.doubleValue();
        }
        if (obj instanceof Boolean bobj) {
            return bobj ? 1 : 0;
        }
        if (obj instanceof StatisticalTest tobj) {
            return tobj.getPvalue();
        }
        if (obj instanceof RegressionItem robj) {
            return robj.getCoefficient();
        }
        if (obj instanceof double[] aobj) {
            return aobj.length == 1 ? aobj[0] : Double.NaN;
        }

        return Double.NaN;
    }

}
