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

import java.util.Arrays;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.toolkit.base.api.modelling.highfreq.HolidaysSpec;
import jdplus.toolkit.base.api.timeseries.calendars.HolidaysOption;
import jdplus.toolkit.base.api.timeseries.regression.HolidaysVariable;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class HolidaysSpecMapping {

    final String HOLIDAYS = "holidays", OPTION = "option",
            SINGLE = "single", NONWORKING = "nonworking", TEST = "test",
            COEF = "coef";

    public InformationSet write(HolidaysSpec spec, boolean verbose) {
        if (!verbose && !spec.isUsed()) {
            return null;
        }
        InformationSet hinfo = new InformationSet();

        writeProperties(hinfo, spec, verbose, true);

        Parameter[] coef = spec.getCoefficients();
        if (coef != null) {
            hinfo.set(COEF, coef);
        }
        return hinfo;
    }

    public void writeProperties(InformationSet hinfo, HolidaysSpec spec, boolean verbose, boolean v3) {
        if (spec.getHolidays() != null) {
            hinfo.add(HOLIDAYS, spec.getHolidays());
        }
        if (verbose || spec.getHolidaysOption() != HolidaysSpec.DEF_OPTION) {
            hinfo.add(OPTION, spec.getHolidaysOption().name());
        }
        if (verbose || spec.isSingle()) {
            hinfo.add(SINGLE, spec.isSingle());
        }
        if (verbose || !Arrays.equals(spec.getNonWorkingDays(), HolidaysVariable.NONWORKING_WE)) {
            hinfo.add(NONWORKING, spec.getNonWorkingDays());
        }
        if (verbose || spec.isTest()) {
            hinfo.add(TEST, spec.isTest());
        }
    }

    public HolidaysSpec read(InformationSet hinfo) {
        if (hinfo == null) {
            return HolidaysSpec.DEFAULT_UNUSED;
        }
        String holidays = hinfo.get(HOLIDAYS, String.class);
        if (holidays == null) {
            return HolidaysSpec.DEFAULT_UNUSED;
        }
        HolidaysSpec.Builder builder = HolidaysSpec.builder()
                .holidays(holidays);
        String option = hinfo.get(OPTION, String.class);
        if (option != null){
            HolidaysOption o= HolidaysOption.valueOf(option);
            builder.holidaysOption(o);
        }
        Boolean single = hinfo.get(SINGLE, Boolean.class);
        if (single != null){
            builder.single(single);
        }
        int[] nwd=hinfo.get(NONWORKING, int[].class);
        if (nwd != null){
            builder.nonWorkingDays(nwd);
        }
        Boolean test = hinfo.get(TEST, Boolean.class);
        if (test != null && test) {
            builder.test(true);
        } else {
            Parameter[] coef = hinfo.get(COEF, Parameter[].class);
            if (coef != null) {
                builder.coefficients(coef);
            }
        }

        return builder.build();
    }

}
