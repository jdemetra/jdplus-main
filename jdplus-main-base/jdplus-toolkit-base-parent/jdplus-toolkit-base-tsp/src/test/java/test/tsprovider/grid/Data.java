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
package test.tsprovider.grid;

import jdplus.toolkit.base.api.timeseries.*;
import jdplus.toolkit.base.tsp.grid.GridLayout;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class Data {

    public final LocalDateTime JAN_ = LocalDate.of(2010, 1, 1).atStartOfDay();
    public final LocalDateTime FEB_ = LocalDate.of(2010, 2, 1).atStartOfDay();
    public final LocalDateTime MAR_ = LocalDate.of(2010, 3, 1).atStartOfDay();

    public final ArrayGridInput EMPTY = ArrayGridInput.of(new Object[][]{});

    public final ArrayGridInput HGRID = ArrayGridInput.of(new Object[][]{
            {null, JAN_, FEB_, MAR_},
            {"S1", 3.14, 4.56, 7.89}
    });

    public final ArrayGridInput HGRID_GAP = ArrayGridInput.of(new Object[][]{
            {null, JAN_, FEB_, MAR_, null},
            {null, null, null, null, null},
            {"S1", 3.14, 4.56, 7.89, null},
            {null, null, null, null, null},
    });

    public final ArrayGridInput HGRID_OVERFLOW = ArrayGridInput.of(new Object[][]{
            {null, JAN_, FEB_, MAR_},
            {"S1", 3.14, 4.56, 7.89, 666}
    });

    public final ArrayGridInput HGRID_UNDERFLOW = ArrayGridInput.of(new Object[][]{
            {null, JAN_, FEB_, MAR_},
            {"S1", 3.14, 4.56}
    });

    public final ArrayGridInput HGRID_NULL_NAME = ArrayGridInput.of(new Object[][]{
            {null, JAN_, FEB_, MAR_},
            {"S1", 3.14, 4.56, 7.89},
            {null, 3333, 4444, 5555}
    });

    public final ArrayGridInput HGRID_CORNER_LABEL = ArrayGridInput.of(new Object[][]{
            {"Dt", JAN_, FEB_, MAR_},
            {"S1", 3.14, 4.56, 7.89}
    });

    public final ArrayGridInput HGRID_NO_NAME = ArrayGridInput.of(new Object[][]{
            {JAN_, FEB_, MAR_},
            {3.14, 4.56, 7.89}
    });

    public final ArrayGridInput HGRID_MULTI_NAME = ArrayGridInput.of(new Object[][]{
            {null, null, JAN_, FEB_, MAR_},
            {"G1", "S1", 3.14, 4.56, 7.89},
            {null, "S2", 1003, 1004, 1005},
            {"G2", "S1", 1007, 1008, 1009},
            {"S1", null, 1000, 1001, 1002}
    });

    public final ArrayGridInput VGRID = ArrayGridInput.of(new Object[][]{
            {null, "S1"},
            {JAN_, 3.14},
            {FEB_, 4.56},
            {MAR_, 7.89}
    });

    public final ArrayGridInput VGRID_GAP1 = ArrayGridInput.of(new Object[][]{
            {null, null, "S1", null},
            {JAN_, null, 3.14, null},
            {FEB_, null, 4.56, null},
            {MAR_, null, 7.89, null},
            {null, null, null, null}
    });

    public final ArrayGridInput VGRID_GAP2 = ArrayGridInput.of(new Object[][]{
            {null, null, null, null},
            {null, null, "S1", null},
            {JAN_, null, 3.14, null},
            {FEB_, null, 4.56, null},
            {MAR_, null, 7.89, null},
            {null, null, null, null}
    });

    public final ArrayGridInput VGRID_GAP3 = ArrayGridInput.of(new Object[][]{
            {null, null, "G1", null},
            {null, null, "S1", null},
            {JAN_, null, 3.14, null},
            {FEB_, null, 4.56, null},
            {MAR_, null, 7.89, null},
            {null, null, null, null}
    });

    public final ArrayGridInput VGRID_OVERFLOW = ArrayGridInput.of(new Object[][]{
            {null, "S1"},
            {JAN_, 3.14},
            {FEB_, 4.56},
            {MAR_, 7.89},
            {null, 666}
    });

    public final ArrayGridInput VGRID_UNDERFLOW = ArrayGridInput.of(new Object[][]{
            {null, "S1"},
            {JAN_, 3.14},
            {FEB_, 4.56},
            {MAR_}
    });

    public final ArrayGridInput VGRID_NULL_NAME = ArrayGridInput.of(new Object[][]{
            {null, "S1", null},
            {JAN_, 3.14, 3333},
            {FEB_, 4.56, 4444},
            {MAR_, 7.89, 5555}
    });

    public final ArrayGridInput VGRID_CORNER_LABEL = ArrayGridInput.of(new Object[][]{
            {"Dt", "S1"},
            {JAN_, 3.14},
            {FEB_, 4.56},
            {MAR_, 7.89}
    });

    public final ArrayGridInput VGRID_NO_NAME = ArrayGridInput.of(new Object[][]{
            {JAN_, 3.14},
            {FEB_, 4.56},
            {MAR_, 7.89}
    });

    public final ArrayGridInput VGRID_MULTI_NAME = ArrayGridInput.of(new Object[][]{
            {null, "G1", null, "G2", "S1"},
            {null, "S1", "S2", "S1", null},
            {JAN_, 3.14, 1003, 1007, 1000},
            {FEB_, 4.56, 1004, 1008, 1001},
            {MAR_, 7.89, 1005, 1009, 1002}
    });

    public static TsData d(TsUnit freq, int year, int position, double... values) {
        TsPeriod p = TsPeriod.yearly(year).withUnit(freq).plus(position);
        return TsData.ofInternal(p, values);
    }

    public static Ts s(String name, TsUnit freq, int year, int position, double... values) {
        return s(name, d(freq, year, position, values));
    }

    public static Ts s(String name, TsData data) {
        return Ts.builder()
                .moniker(TsMoniker.NULL)
                .type(TsInformationType.Data)
                .name(name)
                .data(data)
                .build();
    }

    public static TsCollection c(GridLayout layout, Ts ts) {
        return TsCollection.builder()
                .type(TsInformationType.Data)
                .meta(GridLayout.PROPERTY, layout.name())
                .item(ts)
                .build();
    }
}
