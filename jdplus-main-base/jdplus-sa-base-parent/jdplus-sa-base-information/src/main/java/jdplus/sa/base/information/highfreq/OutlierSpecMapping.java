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

import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.toolkit.base.api.modelling.highfreq.OutlierSpec;
import jdplus.toolkit.base.api.timeseries.TimeSelector;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class OutlierSpecMapping {

    final String SPAN = "span",
            AO = "ao", LS = "ls", WO = "wo",
            VA = "va";

    public InformationSet write(OutlierSpec spec, boolean verbose) {
        if (!verbose && spec.isDefault()) {
            return null;
        }
        InformationSet info = new InformationSet();
        TimeSelector span = spec.getSpan();
        if (verbose || span.getType() != TimeSelector.SelectionType.All) {
            info.add(SPAN, span);
        }
        if (spec.isAo() || verbose) {
            info.add(AO, spec.isAo());
        }
        if (spec.isLs() || verbose) {
            info.add(LS, spec.isLs());
        }
        if (spec.isWo() || verbose) {
            info.add(WO, spec.isWo());
        }
        double cv = spec.getCriticalValue();
        if (verbose || cv != 0) {
            info.add(VA, cv);
        }
        return info;
    }

    public OutlierSpec read(InformationSet info) {
        if (info == null) {
            return OutlierSpec.DEFAULT_DISABLED;
        }

        OutlierSpec.Builder builder = OutlierSpec.builder();

        TimeSelector span = info.get(SPAN, TimeSelector.class);
        if (span != null) {
            builder = builder.span(span);
        }
        Boolean ao = info.get(AO, Boolean.class);
        if (ao != null) {
            builder.ao(ao);
        }
        Boolean ls = info.get(LS, Boolean.class);
        if (ls != null) {
            builder.ls(ls);
        }
        Boolean wo = info.get(WO, Boolean.class);
        if (wo != null) {
            builder.wo(wo);
        }
        Double cv = info.get(VA, Double.class);
        if (cv != null) {
            builder = builder.criticalValue(cv);
        }
        return builder.build();
    }

}
