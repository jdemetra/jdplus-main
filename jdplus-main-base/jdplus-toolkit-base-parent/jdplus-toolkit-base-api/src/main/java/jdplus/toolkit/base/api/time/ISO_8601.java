/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
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
package jdplus.toolkit.base.api.time;

/**
 * <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a> is an international standard covering the worldwide exchange and communication of date and time-related data.
 * The standard provides a well-defined, unambiguous method of representing calendar dates and times.
 * ISO 8601 applies to {@link java.time.LocalDate dates}, {@link java.time.LocalTime times}, {@link java.time.LocalDateTime datetimes},
 * {@link java.time.temporal.TemporalAmount durations}, {@link TimeInterval time intervals} and {@link TimeRecurrence time recurrences}.
 * <p>
 * Date and time values are ordered from the largest to smallest unit of time.
 * This lexicographical order of the representation corresponds to chronological order and allows dates to be naturally sorted.
 *
 * @author Philippe Charles
 */
public @interface ISO_8601 {
}
