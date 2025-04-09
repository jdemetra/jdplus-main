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
@Development(status = Development.Status.Release)
@lombok.Getter
@lombok.AllArgsConstructor
public class Calendar implements CalendarDefinition {

    @lombok.NonNull
    private Holiday[] holidays;

    private boolean meanCorrection;

    public boolean isempty() {
        return holidays.length == 0;
    }

    public static final Calendar DEFAULT = new Calendar(new Holiday[0], true);

    public Calendar(Holiday[] holidays) {
        this.holidays = holidays.clone();
        this.meanCorrection = true;
    }

    public Calendar withMeanCorrection(boolean mean) {
        if (mean == this.meanCorrection) {
            return this;
        } else {
            return new Calendar(holidays, mean);
        }
    }

}
