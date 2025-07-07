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
package jdplus.toolkit.desktop.plugin.core.properties;

import jdplus.toolkit.base.api.timeseries.TsUnit;
import org.openide.nodes.PropertyEditorRegistration;

import java.beans.PropertyEditorSupport;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Philippe Charles
 */
@PropertyEditorRegistration(targetType = TsUnit.class)
public final class TsUnitPropertyEditor extends PropertyEditorSupport {

    @Override
    public String getAsText() {
        TsUnit unit = (TsUnit) getValue();
        String label = UNIT_LABELS.get(unit);
        return unit + (label != null ? (" (" + label + ")") : "");
    }

    private static final Map<TsUnit, String> UNIT_LABELS = initUnitLabels();

    // TODO: put this in a properties file and/or create a special formatter ?
    private static Map<TsUnit, String> initUnitLabels() {
        Map<TsUnit, String> result = new HashMap<>();
        result.put(TsUnit.P1Y, "Yearly");
        result.put(TsUnit.P6M, "HalfYearly");
        result.put(TsUnit.P4M, "QuadriMonthly");
        result.put(TsUnit.P3M, "Quarterly");
        result.put(TsUnit.P2M, "BiMonthly");
        result.put(TsUnit.P1M, "Monthly");
        result.put(TsUnit.P1W, "Weekly");
        result.put(TsUnit.P7D, "Weekly");
        result.put(TsUnit.P1D, "Daily");
        result.put(TsUnit.PT1H, "Daily");
        result.put(TsUnit.PT1M, "Minutely");
        result.put(TsUnit.PT1S, "Secondly");
        return result;
    }
}
