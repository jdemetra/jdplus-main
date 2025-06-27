/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.toolkit.base.api.timeseries;

import jdplus.toolkit.base.api.util.HasShortStringRepresentation;
import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;

import java.time.LocalDate;

/**
 * @author Jean Palate
 */
@RepresentableAsString
@lombok.Value(staticConstructor = "of")
public class CalendarPeriodObs implements TimeSeriesObs<CalendarPeriod>, HasShortStringRepresentation {

    @lombok.NonNull
    LocalDate start, end;

    double value;

    @StaticFactoryMethod
    public static @NonNull CalendarPeriodObs of(@NonNull CalendarPeriod period, double value) {
        return new CalendarPeriodObs(period.getStart(), period.getEnd(), value);
    }

    @StaticFactoryMethod
    public static @NonNull CalendarPeriodObs parse(@NonNull CharSequence text) {
        int index = text.toString().indexOf("=");
        if (index < 0) {
            throw new IllegalArgumentException("Invalid CalendarPeriodObs text: " + text);
        }
        return of(
                CalendarPeriod.parse(text.subSequence(0, index)),
                Double.parseDouble(text.subSequence(index + 1, text.length()).toString())
        );
    }

    @Override
    public @NonNull CalendarPeriod getPeriod() {
        return CalendarPeriod.of(start, end);
    }

    @Override
    public String toString() {
        return getPeriod() + "=" + value;
    }

    @Override
    public @NonNull String toShortString() {
        return getPeriod().toShortString() + "=" + value;
    }
}
