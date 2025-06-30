/*
 * Copyright 2013 National Bank of Belgium
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
package jdplus.toolkit.base.tsp.grid;

import jdplus.toolkit.base.api.timeseries.TsCollection;
import jdplus.toolkit.base.api.timeseries.TsInformationType;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static jdplus.toolkit.base.tsp.grid.GridLayout.HORIZONTAL;
import static jdplus.toolkit.base.tsp.grid.GridLayout.VERTICAL;
import static org.assertj.core.api.Assertions.assertThat;
import static test.tsprovider.grid.Data.*;

/**
 * @author Philippe Charles
 */
public class GridReaderTest {

    @Test
    public void testReadEmpty() throws IOException {
        GridReader x = GridReader.DEFAULT.toBuilder().namePattern("Series ${index}").build();

        assertThat(x.read(EMPTY))
                .isEqualTo(TsCollection
                        .builder()
                        .meta(GridLayout.PROPERTY, VERTICAL.name())
                        .type(TsInformationType.Data)
                        .build());
    }

    @Test
    public void testReadHorizontal() throws IOException {
        GridReader x = GridReader.DEFAULT.toBuilder().namePattern("Series ${index}").build();

        for (GridInput o : new GridInput[]{HGRID, HGRID_GAP, HGRID_OVERFLOW, HGRID_CORNER_LABEL}) {
            assertThat(x.read(o)).isEqualTo(c(HORIZONTAL, s("S1", TsUnit.P1M, 2010, 0, 3.14, 4.56, 7.89)));
        }
        assertThat(x.read(HGRID_UNDERFLOW))
                .isEqualTo(c(HORIZONTAL, s("S1", TsUnit.P1M, 2010, 0, 3.14, 4.56)));

        assertThat(x.read(HGRID_NO_NAME))
                .isEqualTo(c(HORIZONTAL, s("Series 0", TsUnit.P1M, 2010, 0, 3.14, 4.56, 7.89)));

        assertThat(x.toBuilder().namePattern("X${number}").build().read(HGRID_NO_NAME))
                .isEqualTo(c(HORIZONTAL, s("X1", TsUnit.P1M, 2010, 0, 3.14, 4.56, 7.89)));

        assertThat(x.read(HGRID_NULL_NAME))
                .isEqualTo(TsCollection
                        .builder()
                        .meta(GridLayout.PROPERTY, HORIZONTAL.name())
                        .type(TsInformationType.Data)
                        .item(s("S1", TsUnit.P1M, 2010, 0, 3.14, 4.56, 7.89))
                        .item(s("null", TsUnit.P1M, 2010, 0, 3333, 4444, 5555))
                        .build());

        assertThat(x.read(HGRID_MULTI_NAME))
                .isEqualTo(TsCollection
                        .builder()
                        .meta(GridLayout.PROPERTY, HORIZONTAL.name())
                        .type(TsInformationType.Data)
                        .item(s("G1\nS1", TsUnit.P1M, 2010, 0, 3.14, 4.56, 7.89))
                        .item(s("G1\nS2", TsUnit.P1M, 2010, 0, 1003, 1004, 1005))
                        .item(s("G2\nS1", TsUnit.P1M, 2010, 0, 1007, 1008, 1009))
                        .item(s("S1", TsUnit.P1M, 2010, 0, 1000, 1001, 1002))
                        .build());
    }

    @Test
    public void testReadVertical() throws IOException {
        GridReader x = GridReader.DEFAULT.toBuilder().namePattern("Series ${index}").build();

        for (GridInput o : new GridInput[]{VGRID, VGRID_GAP1, VGRID_GAP2, VGRID_OVERFLOW, VGRID_CORNER_LABEL}) {
            assertThat(x.read(o)).isEqualTo(c(VERTICAL, s("S1", TsUnit.P1M, 2010, 0, 3.14, 4.56, 7.89)));
        }

        assertThat(x.read(VGRID_GAP3))
                .isEqualTo(c(VERTICAL, s("G1\nS1", TsUnit.P1M, 2010, 0, 3.14, 4.56, 7.89)));

        assertThat(x.read(VGRID_UNDERFLOW))
                .isEqualTo(c(VERTICAL, s("S1", TsUnit.P1M, 2010, 0, 3.14, 4.56)));

        assertThat(x.read(VGRID_NO_NAME))
                .isEqualTo(c(VERTICAL, s("Series 0", TsUnit.P1M, 2010, 0, 3.14, 4.56, 7.89)));

        assertThat(x.toBuilder().namePattern("X${number}").build().read(VGRID_NO_NAME))
                .isEqualTo(c(VERTICAL, s("X1", TsUnit.P1M, 2010, 0, 3.14, 4.56, 7.89)));

        assertThat(x.read(VGRID_NULL_NAME))
                .isEqualTo(TsCollection
                        .builder()
                        .meta(GridLayout.PROPERTY, VERTICAL.name())
                        .type(TsInformationType.Data)
                        .item(s("S1", TsUnit.P1M, 2010, 0, 3.14, 4.56, 7.89))
                        .item(s("null", TsUnit.P1M, 2010, 0, 3333, 4444, 5555))
                        .build());

        assertThat(x.read(VGRID_MULTI_NAME))
                .isEqualTo(TsCollection
                        .builder()
                        .meta(GridLayout.PROPERTY, VERTICAL.name())
                        .type(TsInformationType.Data)
                        .item(s("G1\nS1", TsUnit.P1M, 2010, 0, 3.14, 4.56, 7.89))
                        .item(s("G1\nS2", TsUnit.P1M, 2010, 0, 1003, 1004, 1005))
                        .item(s("G2\nS1", TsUnit.P1M, 2010, 0, 1007, 1008, 1009))
                        .item(s("S1", TsUnit.P1M, 2010, 0, 1000, 1001, 1002))
                        .build());
    }

    @Test
    public void testNameSeparator() throws IOException {
        GridReader x = GridReader.DEFAULT.toBuilder().nameSeparator("-").build();

        assertThat(x.read(VGRID_MULTI_NAME))
                .isEqualTo(TsCollection
                        .builder()
                        .meta(GridLayout.PROPERTY, VERTICAL.name())
                        .type(TsInformationType.Data)
                        .item(s("G1-S1", TsUnit.P1M, 2010, 0, 3.14, 4.56, 7.89))
                        .item(s("G1-S2", TsUnit.P1M, 2010, 0, 1003, 1004, 1005))
                        .item(s("G2-S1", TsUnit.P1M, 2010, 0, 1007, 1008, 1009))
                        .item(s("S1", TsUnit.P1M, 2010, 0, 1000, 1001, 1002))
                        .build());
    }

    @Test
    public void testLayout() throws IOException {
        assertThat(GridReader.builder().layout(HORIZONTAL).build().read(HGRID_MULTI_NAME))
                .isEqualTo(TsCollection
                        .builder()
                        .meta(GridLayout.PROPERTY, HORIZONTAL.name())
                        .type(TsInformationType.Data)
                        .item(s("G1\nS1", TsUnit.P1M, 2010, 0, 3.14, 4.56, 7.89))
                        .item(s("G1\nS2", TsUnit.P1M, 2010, 0, 1003, 1004, 1005))
                        .item(s("G2\nS1", TsUnit.P1M, 2010, 0, 1007, 1008, 1009))
                        .item(s("S1", TsUnit.P1M, 2010, 0, 1000, 1001, 1002))
                        .build());

        assertThat(GridReader.builder().layout(VERTICAL).build().read(VGRID_MULTI_NAME))
                .isEqualTo(TsCollection
                        .builder()
                        .meta(GridLayout.PROPERTY, VERTICAL.name())
                        .type(TsInformationType.Data)
                        .item(s("G1\nS1", TsUnit.P1M, 2010, 0, 3.14, 4.56, 7.89))
                        .item(s("G1\nS2", TsUnit.P1M, 2010, 0, 1003, 1004, 1005))
                        .item(s("G2\nS1", TsUnit.P1M, 2010, 0, 1007, 1008, 1009))
                        .item(s("S1", TsUnit.P1M, 2010, 0, 1000, 1001, 1002))
                        .build());
    }
}
