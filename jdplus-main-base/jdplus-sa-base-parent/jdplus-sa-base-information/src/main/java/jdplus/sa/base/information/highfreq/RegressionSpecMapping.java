/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.sa.base.information.highfreq;

import jdplus.toolkit.base.api.information.Information;
import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.toolkit.base.information.VariableMapping;
import jdplus.toolkit.base.api.modelling.highfreq.RegressionSpec;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.regression.IOutlier;
import jdplus.toolkit.base.api.timeseries.regression.InterventionVariable;
import jdplus.toolkit.base.api.timeseries.regression.TsContextVariable;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import java.util.List;
import jdplus.toolkit.base.api.modelling.highfreq.EasterSpec;
import jdplus.toolkit.base.api.modelling.highfreq.HolidaysSpec;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class RegressionSpecMapping {

    final String CAL = "calendar", EASTER = "easter",
            OUTLIER = "outlier", OUTLIERS = "outlier*",
            USER = "user", USERS = "user*",
            INTERVENTION = "intervention", INTERVENTIONS = "intervention*";

    public RegressionSpec read(InformationSet info) {
        if (info == null) {
            return RegressionSpec.DEFAULT;
        }
        RegressionSpec.Builder builder = RegressionSpec.builder();

        HolidaysSpec hol = HolidaysSpecMapping.read(info.getSubSet(CAL));
        if (hol != null) {
            builder.holidays(hol);
        }
        EasterSpec e=EasterSpecMapping.read(info.getSubSet(EASTER));
        if (e != null){
            builder.easter(e);
        }
        List<Information<InformationSet>> sel = info.select(OUTLIERS, InformationSet.class);
        if (!sel.isEmpty()) {
            for (Information<InformationSet> sub : sel) {
                Variable<IOutlier> v = VariableMapping.readO(sub.getValue());
                builder.outlier(v);
            }
        }
        sel = info.select(INTERVENTIONS, InformationSet.class);
        if (!sel.isEmpty()) {
            for (Information<InformationSet> sub : sel) {
                Variable<InterventionVariable> v = VariableMapping.readIV(sub.getValue());
                builder.interventionVariable(v);
            }
        }
        sel = info.select(USERS, InformationSet.class);
        if (!sel.isEmpty()) {
            for (Information<InformationSet> sub : sel) {
                Variable<TsContextVariable> v = VariableMapping.readT(sub.getValue());
                builder.userDefinedVariable(v);
            }
        }
        return builder.build();
    }

    public InformationSet write(RegressionSpec spec, TsDomain context, boolean verbose) {
        if (!spec.isUsed()) {
            return null;
        }
        InformationSet info = new InformationSet();

        InformationSet hinfo = HolidaysSpecMapping.write(spec.getHolidays(), verbose);
        if (hinfo != null) {
            info.set(CAL, hinfo);
        }
        InformationSet einfo = EasterSpecMapping.write(spec.getEaster(), verbose);
        if (einfo != null) {
            info.set(EASTER, einfo);
        }
        List<Variable<IOutlier>> voutliers = spec.getOutliers();
        if (!voutliers.isEmpty()) {
            int idx = 1;
            for (Variable<IOutlier> v : voutliers) {
                InformationSet w = VariableMapping.writeO(v, verbose);
                info.set(OUTLIER + (idx++), w);
            }
        }
        List<Variable<TsContextVariable>> vusers = spec.getUserDefinedVariables();
        if (!vusers.isEmpty()) {
            int idx = 1;
            for (Variable<TsContextVariable> v : vusers) {
                InformationSet w = VariableMapping.writeT(v, verbose);
                info.set(USER + (idx++), w);
            }
        }
        List<Variable<InterventionVariable>> viv = spec.getInterventionVariables();
        if (!viv.isEmpty()) {
            int idx = 1;
            for (Variable<InterventionVariable> v : viv) {
                InformationSet w = VariableMapping.writeIV(v, verbose);
                info.set(INTERVENTION + (idx++), w);
            }
        }
        return info;
    }

}
