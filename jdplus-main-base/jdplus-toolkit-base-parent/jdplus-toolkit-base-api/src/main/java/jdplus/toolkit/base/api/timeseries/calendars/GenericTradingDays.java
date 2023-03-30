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
package jdplus.toolkit.base.api.timeseries.calendars;

import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@Development(status = Development.Status.Alpha)
public class GenericTradingDays {
    
//    public static enum Type{
//        /**
//         * Number of days in each group
//         */
//        RAW,
//        /**
//         * Number of days in group(i)/#group[i)*#group(0) - numbers of days in group(0)
//         */
//        CONTRAST,
//        /**
//         * Contrasts corrected for long term effects:
//         * group(i)- group(0) - (avg(Number of days in group(i))-avg(Number of days in group(0)))
//         * = (group(i)-avg(Number of days in group(i)))-(group(0)-avg(Number of days in group(0)))
//         */
//        MEANCORRECTEDCONTRAST
//    }

    private DayClustering clustering;
    private boolean contrast;
    
    public static GenericTradingDays raw(DayClustering clustering){
        return new GenericTradingDays(clustering, false);
    }

    public static GenericTradingDays contrasts(DayClustering clustering){
        return new GenericTradingDays(clustering, true);
    }
 


    public int getCount() {
        int n = clustering.getGroupsCount();
        return contrast ? n - 1 : n;
    }

    public String getDescription(int idx) {
        return clustering.toString(idx);
    }

}
