package jdplus.spreadsheet.base.api.sa;

import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.helpers.ArrayBook;
import ec.util.spreadsheet.helpers.ArraySheet;
import internal.spreadsheet.base.api.SpreadsheetManager;
import jdplus.sa.base.api.SaDocument;
import jdplus.toolkit.base.api.processing.GenericOutput;
import jdplus.toolkit.base.api.timeseries.Ts;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import lombok.NonNull;
import nbbrd.design.MightBePromoted;
import org.assertj.core.util.DateUtil;
import org.junit.Test;
import tck.demetra.data.Data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.AbstractList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static jdplus.spreadsheet.base.api.sa.SpreadsheetOutputConfiguration.SpreadsheetLayout.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;

public class SpreadsheetOutputTest {

    @Test
    public void testBySeriesNoKeys() throws Exception {
        SpreadsheetOutputConfiguration config = new SpreadsheetOutputConfiguration();
        config.setLayout(BySeries);
        config.setSeries(List.of());

        assertThat(write(config)).isEmpty();

        assertThat(write(config, DOC1, DOC2, DOC3)).extracting(ArraySheet::getName)
                .containsExactly("Series0", "Series1", "Series2");

        assertThat(write(config, DOC1, DOC2, DOC3)).extracting(SpreadsheetOutputTest::toTable)
                .hasSize(3)
                .contains(
                        new Object[][]{
                                {null, D1__},
                        }, atIndex(0))
                .contains(
                        new Object[][]{
                                {null, D2__},
                        }, atIndex(1))
                .contains(
                        new Object[][]{
                                {null, D3__},
                        }, atIndex(2));
    }

    @Test
    public void testBySeriesAnyKeys() throws Exception {
        SpreadsheetOutputConfiguration config = new SpreadsheetOutputConfiguration();
        config.setLayout(BySeries);
        config.setSeries(List.of(__Q_, __M_));

        assertThat(write(config)).isEmpty();

        assertThat(write(config, DOC1, DOC2, DOC3)).extracting(ArraySheet::getName)
                .containsExactly("Series0", "Series1", "Series2");

        assertThat(write(config, DOC1, DOC2, DOC3)).extracting(SpreadsheetOutputTest::toTable)
                .hasSize(3)
                .contains(
                        new Object[][]{
                                {null, D1__, null},
                                {null, __Q_, __M_},
                                {_JAN, D1Q1, D1M1},
                                {_FEB, null, D1M2},
                                {_MAR, null, null},
                        }, atIndex(0))
                .contains(
                        new Object[][]{
                                {null, D2__, null},
                                {null, __Q_, __M_},
                                {_FEB, null, D2M2},
                        }, atIndex(1))
                .contains(
                        new Object[][]{
                                {null, D3__, null},
                                {null, __Q_, __M_},
                        }, atIndex(2));
    }

    @Test
    public void testByComponentNoKeys() throws Exception {
        SpreadsheetOutputConfiguration config = new SpreadsheetOutputConfiguration();
        config.setLayout(ByComponent);
        config.setSeries(List.of());

        assertThat(write(config)).isEmpty();
        assertThat(write(config, DOC1, DOC2, DOC3)).isEmpty();
    }

    @Test
    public void testByComponentAnyKeys() throws Exception {
        SpreadsheetOutputConfiguration config = new SpreadsheetOutputConfiguration();
        config.setLayout(ByComponent);
        config.setSeries(List.of(__Q_, __M_));

        assertThat(write(config)).isEmpty();

        assertThat(write(config, DOC1, DOC2, DOC3)).extracting(ArraySheet::getName)
                .containsExactly(__Q_, __M_);

        assertThat(write(config, DOC1, DOC2, DOC3)).extracting(SpreadsheetOutputTest::toTable)
                .hasSize(2)
                .contains(
                        new Object[][]{
                                {null, __Q_, null, null},
                                {null, D1__, D2__, D3__},
                                {_JAN, D1Q1, null, null},
                        }, atIndex(0))
                .contains(
                        new Object[][]{
                                {null, __M_, null, null},
                                {null, D1__, D2__, D3__},
                                {_JAN, D1M1, null, null},
                                {_FEB, D1M2, D2M2, null},
                        }, atIndex(1));
    }

    @Test
    public void testOneSheetNoKeys() throws Exception {
        SpreadsheetOutputConfiguration config = new SpreadsheetOutputConfiguration();
        config.setLayout(OneSheet);
        config.setSeries(List.of());

        assertThat(write(config)).extracting(ArraySheet::getName)
                .containsExactly("Series");

        assertThat(write(config)).extracting(SpreadsheetOutputTest::toTable)
                .hasSize(1)
                .contains(new Object[][]{
                }, atIndex(0));

        assertThat(write(config, DOC1, DOC2, DOC3)).extracting(ArraySheet::getName)
                .containsExactly("Series");

        assertThat(write(config, DOC1, DOC2, DOC3)).extracting(SpreadsheetOutputTest::toTable)
                .hasSize(1)
                .contains(new Object[][]{
                        {null, D1__, D2__, D3__},
                }, atIndex(0));
    }

    @Test
    public void testOneSheetAnyKeys() throws Exception {
        SpreadsheetOutputConfiguration config = new SpreadsheetOutputConfiguration();
        config.setLayout(OneSheet);
        config.setSeries(List.of(__Q_, __M_));

        assertThat(write(config)).extracting(ArraySheet::getName)
                .containsExactly("Series");

        assertThat(write(config)).extracting(SpreadsheetOutputTest::toTable)
                .hasSize(1)
                .contains(new Object[][]{
                }, atIndex(0));

        assertThat(write(config, DOC1, DOC2, DOC3)).extracting(ArraySheet::getName)
                .containsExactly("Series");

        assertThat(write(config, DOC1, DOC2, DOC3)).extracting(SpreadsheetOutputTest::toTable)
                .hasSize(1)
                .contains(new Object[][]{
                        {null, D1__, null, D2__, null, D3__, null},
                        {null, __Q_, __M_, __Q_, __M_, __Q_, __M_},
                        {_JAN, D1Q1, D1M1, null, null, null, null},
                        {_FEB, null, D1M2, null, D2M2, null, null},
                        {_MAR, null, null, null, null, null, null},
                }, atIndex(0));
    }

    private static List<ArraySheet> write(SpreadsheetOutputConfiguration config, SaDocument... docs) throws Exception {
        AtomicReference<ArrayBook> storage = new AtomicReference<>();
        SpreadsheetManager manager = SpreadsheetManager.builder().factory(new MockedFactory(storage)).build();
        SpreadsheetOutput output = new SpreadsheetOutput(config, manager);
        output.start(null);
        for (SaDocument doc : docs) {
            output.process(doc);
        }
        output.end(null);
        return asList(storage.get());
    }

    @MightBePromoted
    private static List<ArraySheet> asList(ArrayBook book) {
        return new AbstractList<>() {
            @Override
            public ArraySheet get(int index) {
                return book.getSheet(index);
            }

            @Override
            public int size() {
                return book.getSheetCount2();
            }
        };
    }

    @MightBePromoted
    private static Object[][] toTable(ArraySheet sheet) {
        Object[][] result = new Object[sheet.getRowCount()][];
        for (int i = 0; i < sheet.getRowCount(); i++) {
            Object[] row = new Object[sheet.getColumnCount()];
            for (int j = 0; j < sheet.getColumnCount(); j++) {
                row[j] = sheet.getCellValue(i, j);
            }
            result[i] = row;
        }
        return result;
    }

    @lombok.AllArgsConstructor
    private static class MockedFactory extends Book.Factory {

        private final @NonNull AtomicReference<ArrayBook> storage;

        @Override
        public boolean accept(File pathname) {
            return true;
        }

        @Override
        public @NonNull String getName() {
            return "demo";
        }

        @Override
        public @NonNull Book load(@NonNull InputStream stream) throws IOException {
            ArrayBook result = storage.get();
            if (result == null) throw new IOException("Storage empty");
            return result;
        }

        @Override
        public void store(@NonNull OutputStream stream, @NonNull Book book) throws IOException {
            storage.set(ArrayBook.copyOf(book));
        }
    }

    private static final String D1__ = "full";
    private static final String D2__ = "partial";
    private static final String D3__ = "empty";
    private static final String __M_ = "monthly";
    private static final String __Q_ = "quarterly";
    private static final String __Y_ = "yearly";
    private static final Date _JAN = DateUtil.parse("2010-01-01");
    private static final Date _FEB = DateUtil.parse("2010-02-01");
    private static final Date _MAR = DateUtil.parse("2010-03-01");
    private static final double D1M1 = 9161;
    private static final double D1M2 = 9162;
    private static final double D1Q1 = 9171;
    private static final double D2M2 = 9262;
    private static final double D1Y1 = 9181;
    private static final Ts TS = Ts.of(Data.TS_PROD);

    private static final SaDocument DOC1 = new SaDocument(
            D1__,
            TS,
            null,
            GenericOutput
                    .builder()
                    .entry(__M_, TsData.ofInternal(TsPeriod.monthly(2010, 1), new double[]{D1M1, D1M2}))
                    .entry(__Q_, TsData.ofInternal(TsPeriod.quarterly(2010, 1), new double[]{D1Q1}))
                    .entry(__Y_, TsData.ofInternal(TsPeriod.yearly(2010), new double[]{D1Y1}))
                    .build(),
            null,
            null
    );

    private static final SaDocument DOC2 = new SaDocument(
            D2__,
            TS,
            null,
            GenericOutput
                    .builder()
                    .entry(__M_, TsData.ofInternal(TsPeriod.monthly(2010, 2), new double[]{D2M2}))
                    .build(),
            null,
            null
    );

    private static final SaDocument DOC3 = new SaDocument(
            D3__,
            TS,
            null,
            GenericOutput
                    .builder()
                    .build(),
            null,
            null
    );
}
