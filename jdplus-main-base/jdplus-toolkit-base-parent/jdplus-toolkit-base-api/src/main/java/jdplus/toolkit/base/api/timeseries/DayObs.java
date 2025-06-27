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

import lombok.NonNull;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;

import java.time.LocalDate;

/**
 * @author Philippe Charles
 */
@RepresentableAsString
@lombok.Value(staticConstructor = "of")
public class DayObs implements TimeSeriesObs<Day> {

    @lombok.NonNull
    LocalDate date;

    double value;

    @Override
    public @NonNull Day getPeriod() {
        return Day.of(date);
    }

    @StaticFactoryMethod
    public static @NonNull DayObs parse(@NonNull CharSequence text) {
        int index = text.toString().indexOf("=");
        if (index < 0) {
            throw new IllegalArgumentException("Invalid DayObs text: " + text);
        }
        return of(
                Day.parse(text.subSequence(0, index)).getDay(),
                Double.parseDouble(text.subSequence(index + 1, text.length()).toString())
        );
    }

    @Override
    public String toString() {
        return getPeriod() + "=" + value;
    }
}
