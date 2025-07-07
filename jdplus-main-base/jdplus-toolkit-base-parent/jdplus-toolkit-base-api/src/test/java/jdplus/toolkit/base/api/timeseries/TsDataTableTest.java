/*
 * Copyright 2017 National Bank of Belgium
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
package jdplus.toolkit.base.api.timeseries;

import jdplus.toolkit.base.api.timeseries.TsDataTable.ValueStatus;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import static java.lang.Double.NaN;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyIterator;
import static jdplus.toolkit.base.api.timeseries.TsDataTable.DistributionType.*;
import static jdplus.toolkit.base.api.timeseries.TsDataTable.ValueStatus.*;
import static jdplus.toolkit.base.api.timeseries.TsDataTable.computeDomain;
import static org.assertj.core.api.Assertions.*;

/**
 * @author Philippe Charles
 */
public class TsDataTableTest {

    @Test
    public void testComputeDomain() {
        assertThat(computeDomain(emptyIterator()))
                .as("no domains")
                .isEqualTo(TsDomain.DEFAULT_EMPTY);

        assertThat(computeDomain(domainsOf("R1/1970/P14M", "R1/1970/P14M")))
                .as("same chrono, same amount")
                .hasToString("R1/1970-01-01T00:00:00/P14M");

        assertThat(computeDomain(domainsOf("R1/1970/P14M", "R1/1970/P7M")))
                .as("same chrono, compatible amount")
                .hasToString("R2/1970-01-01T00:00:00/P7M");

        assertThat(computeDomain(domainsOf("R1/1970/P14M", "R1/1970/P12M")))
                .as("same chrono, incompatible amount")
                .hasToString("R7/1970-01-01T00:00:00/P2M");

        assertThat(computeDomain(domainsOf("R1/1970/P2Y", "R1/1970/P2M")))
                .as("compatible chrono, same amount")
                .hasToString("R12/1970-01-01T00:00:00/P2M");

        assertThat(computeDomain(domainsOf("R1/1970/P2Y", "R1/1970/P12M")))
                .as("compatible chrono, compatible amount")
                .hasToString("R2/1970-01-01T00:00:00/P12M");

        assertThat(computeDomain(domainsOf("R1/1970/P2Y", "R1/1970/P26M")))
                .as("compatible chrono, incompatible amount")
                .hasToString("R13/1970-01-01T00:00:00/P2M");

        assertThat(computeDomain(domainsOf("R1/1970/P2M", "R1/1970/P2D")))
                .as("incompatible chrono, same amount")
                .hasToString("R59/1970-01-01T00:00:00/P1D");

        assertThat(computeDomain(domainsOf("R1/1970/P2M", "R1/1970/P10D")))
                .as("incompatible chrono, compatible amount")
                .hasToString("R59/1970-01-01T00:00:00/P1D");

        assertThat(computeDomain(domainsOf("R1/1970/P2M", "R1/1970/P11D")))
                .as("incompatible chrono, incompatible amount")
                .hasToString("R59/1970-01-01T00:00:00/P1D");
    }

    @Test
    @SuppressWarnings({"null", "DataFlowIssue"})
    public void testFactory() {
        assertThatNullPointerException().isThrownBy(() -> TsDataTable.of(null));

        assertThat(TsDataTable.of(Collections.emptyList()))
                .extracting(TsDataTable::getDomain, TsDataTable::getData)
                .containsExactly(TsDomain.DEFAULT_EMPTY, Collections.emptyList());

        assertThat(TsDataTable.of(asList(empty, empty)))
                .extracting(TsDataTable::getDomain, TsDataTable::getData)
                .containsExactly(TsDomain.DEFAULT_EMPTY, asList(empty, empty));

        assertThat(TsDataTable.of(asList(p1m_jan2010, empty)))
                .extracting(TsDataTable::getDomain, TsDataTable::getData)
                .containsExactly(p1m_jan2010.getDomain(), asList(p1m_jan2010, empty));

        assertThat(TsDataTable.of(asList(p1m_jan2010, null)))
                .returns(p1m_jan2010.getDomain(), TsDataTable::getDomain)
                .returns(asList(p1m_jan2010, null), TsDataTable::getData);

        assertThat(TsDataTable.of(asList(empty, p1m_jan2010)))
                .extracting(TsDataTable::getDomain, TsDataTable::getData)
                .containsExactly(p1m_jan2010.getDomain(), asList(empty, p1m_jan2010));
    }

    @Test
    @SuppressWarnings({"null", "DataFlowIssue"})
    public void testCursorValue() {
        assertThatNullPointerException().isThrownBy(() -> TsDataTable.of(Collections.emptyList()).cursor((TsDataTable.DistributionType) null));
        assertThatNullPointerException().isThrownBy(() -> TsDataTable.of(Collections.emptyList()).cursor((IntFunction<TsDataTable.DistributionType>) null));

        assertThat(Cell.toArray(TsDataTable.of(Collections.emptyList()).cursor(FIRST))).isEmpty();
        assertThat(Cell.toArray(TsDataTable.of(List.of(empty)).cursor(FIRST))).isEmpty();

        TsDataTable table = TsDataTable.of(asList(p1m_jan2010, p3m_oct2009, empty));

        assertThatExceptionOfType(IndexOutOfBoundsException.class).isThrownBy(() -> table.cursor(FIRST).moveTo(0, -1));
        assertThatExceptionOfType(IndexOutOfBoundsException.class).isThrownBy(() -> table.cursor(FIRST).moveTo(-1, 0));
        assertThatExceptionOfType(IndexOutOfBoundsException.class).isThrownBy(() -> table.cursor(FIRST).moveTo(0, 3));
        assertThatExceptionOfType(IndexOutOfBoundsException.class).isThrownBy(() -> table.cursor(FIRST).moveTo(6, 0));

        assertDeepEqualTo(Cell.toArray(table.cursor(FIRST)),
                new Cell[][]{
                        {Cell.of(-3, -1, -1, BEFORE, NaN), Cell.of(0, 3, 0, PRESENT, 2.1), Cell.EMPTY},
                        {Cell.of(-2, -1, -1, BEFORE, NaN), Cell.of(0, 3, 1, UNUSED, NaN), Cell.EMPTY},
                        {Cell.of(-1, -1, -1, BEFORE, NaN), Cell.of(0, 3, 2, UNUSED, NaN), Cell.EMPTY},
                        {Cell.of(0, 1, 0, PRESENT, 1.1), Cell.of(1, -1, -1, AFTER, NaN), Cell.EMPTY},
                        {Cell.of(1, 1, 0, PRESENT, NaN), Cell.of(1, -1, -1, AFTER, NaN), Cell.EMPTY},
                        {Cell.of(2, 1, 0, PRESENT, 1.3), Cell.of(1, -1, -1, AFTER, NaN), Cell.EMPTY}
                });

        assertDeepEqualTo(Cell.toArray(table.cursor(MIDDLE)),
                new Cell[][]{
                        {Cell.of(-3, -1, -1, BEFORE, NaN), Cell.of(0, 3, 0, UNUSED, NaN), Cell.EMPTY},
                        {Cell.of(-2, -1, -1, BEFORE, NaN), Cell.of(0, 3, 1, PRESENT, 2.1), Cell.EMPTY},
                        {Cell.of(-1, -1, -1, BEFORE, NaN), Cell.of(0, 3, 2, UNUSED, NaN), Cell.EMPTY},
                        {Cell.of(0, 1, 0, PRESENT, 1.1), Cell.of(1, -1, -1, AFTER, NaN), Cell.EMPTY},
                        {Cell.of(1, 1, 0, PRESENT, NaN), Cell.of(1, -1, -1, AFTER, NaN), Cell.EMPTY},
                        {Cell.of(2, 1, 0, PRESENT, 1.3), Cell.of(1, -1, -1, AFTER, NaN), Cell.EMPTY}
                });

        assertDeepEqualTo(Cell.toArray(table.cursor(LAST)),
                new Cell[][]{
                        {Cell.of(-3, -1, -1, BEFORE, NaN), Cell.of(0, 3, 0, UNUSED, NaN), Cell.EMPTY},
                        {Cell.of(-2, -1, -1, BEFORE, NaN), Cell.of(0, 3, 1, UNUSED, NaN), Cell.EMPTY},
                        {Cell.of(-1, -1, -1, BEFORE, NaN), Cell.of(0, 3, 2, PRESENT, 2.1), Cell.EMPTY},
                        {Cell.of(0, 1, 0, PRESENT, 1.1), Cell.of(1, -1, -1, AFTER, NaN), Cell.EMPTY},
                        {Cell.of(1, 1, 0, PRESENT, NaN), Cell.of(1, -1, -1, AFTER, NaN), Cell.EMPTY},
                        {Cell.of(2, 1, 0, PRESENT, 1.3), Cell.of(1, -1, -1, AFTER, NaN), Cell.EMPTY}
                });
    }

    private final TsData p1m_jan2010 = TsData.ofInternal(TsPeriod.monthly(2010, 1), new double[]{1.1, Double.NaN, 1.3});
    private final TsData p3m_oct2009 = TsData.ofInternal(TsPeriod.quarterly(2009, 4), new double[]{2.1});
    private final TsData empty = TsData.empty("empty");

    @lombok.Value(staticConstructor = "of")
    private static class Cell {

        static final Cell EMPTY = Cell.of(-1, -1, -1, TsDataTable.ValueStatus.EMPTY, NaN);

        int index;
        int windowLength;
        int windowIndex;
        @lombok.NonNull
        ValueStatus status;
        double value;

        static Cell[][] toArray(TsDataTable.Cursor c) {
            Cell[][] result = new Cell[c.getPeriodCount()][c.getSeriesCount()];
            for (int i = 0; i < c.getPeriodCount(); i++) {
                for (int j = 0; j < c.getSeriesCount(); j++) {
                    c.moveTo(i, j);
                    result[i][j] = Cell.of(c.getIndex(), c.getWindowLength(), c.getWindowIndex(), c.getStatus(), c.getValue());
                }
            }
            return result;
        }
    }

    private void assertDeepEqualTo(Object[][] actual, Object[][] expected) {
        // workaround of bug in assertj 3.17.0
//        assertThat(actual).isDeepEqualTo(expected);
        assertThat(Arrays.deepEquals(actual, expected));
    }

    private Iterator<TsDomain> domainsOf(String... domains) {
        return Stream.of(domains).map(TsDomain::parse).iterator();
    }
}
