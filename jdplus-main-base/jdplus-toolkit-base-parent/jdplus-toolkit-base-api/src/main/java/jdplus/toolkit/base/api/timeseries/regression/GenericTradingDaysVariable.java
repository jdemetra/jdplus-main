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
package jdplus.toolkit.base.api.timeseries.regression;

import java.util.Locale;
import jdplus.toolkit.base.api.timeseries.TimeSeriesDomain;
import nbbrd.design.Development;
import jdplus.toolkit.base.api.timeseries.calendars.DayClustering;
import jdplus.toolkit.base.api.timeseries.calendars.GenericTradingDays;
import jdplus.toolkit.base.api.timeseries.calendars.TradingDaysType;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.AllArgsConstructor
@Development(status = Development.Status.Release)
public class GenericTradingDaysVariable implements ITradingDaysVariable, ISystemVariable {

    private DayClustering clustering;

    public GenericTradingDaysVariable(GenericTradingDays td) {
        this.clustering = td.getClustering();
    }

    @Override
    public int dim() {
        return clustering.getGroupsCount() - 1;
    }

    @Override
    public TradingDaysType getTradingDaysType() {
        return clustering.getType();
    }

    @Override
    public <D extends TimeSeriesDomain<?>> String description(D context) {
        return nameOf(clustering);
    }

    @Override
    public <D extends TimeSeriesDomain<?>> String description(int idx, D context) {
        return description(clustering, idx);
    }

    static final String[] TD2 = new String[]{"week", "week-end"};
    static final String[] TD2c = new String[]{"mon-sat", "sun"};
    static final String[] TD2d = new String[]{"mon-thu", "fri-sat-sun"};
    static final String[] TD7 = new String[]{"mon", "tue", "wed", "thu", "fri", "sat", "sun"};
    static final String[] TD3 = new String[]{"week", "sat", "sun"};
    static final String[] TD3c = new String[]{"mon-thu", "fri-sat", "sun"};
    static final String[] TD4 = new String[]{"mon-thu", "fri", "sat", "sun"};
    static final String[] TD4c = new String[]{"mon", "tue-fri", "sat", "sun"};
    static final String[] TD6 = new String[]{"mon", "tue", "wed", "thu", "fri", "week-end"};

    public static String description(DayClustering dc, int idx) {
        if (dc.equals(DayClustering.TD2)) {
            return TD2[idx];
        } else if (dc.equals(DayClustering.TD7)) {
            return TD7[idx];
        } else if (dc.equals(DayClustering.TD3)) {
            return TD3[idx];
        } else if (dc.equals(DayClustering.TD2c)) {
            return TD2c[idx];
        } else if (dc.equals(DayClustering.TD2d)) {
            return TD2d[idx];
        } else if (dc.equals(DayClustering.TD3c)) {
            return TD3c[idx];
        } else if (dc.equals(DayClustering.TD4)) {
            return TD4[idx];
        } else if (dc.equals(DayClustering.TD4c)) {
            return TD4c[idx];
        } else if (dc.equals(DayClustering.TD6)) {
            return TD6[idx];
        } else {
            return "td-" + (idx + 1);
        }
    }

    public static String nameOf(DayClustering dc) {

        TradingDaysType type = dc.getType();
        if (type != null) {
            return type.name().toLowerCase(Locale.ROOT);
        } else {
            return "td";
        }
    }
}
