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
package jdplus.spreadsheet.base.api;

import ec.util.spreadsheet.Book;
import internal.spreadsheet.base.api.*;
import internal.spreadsheet.base.api.grid.SheetGrid;
import internal.spreadsheet.base.api.legacy.LegacySpreadSheetMoniker;
import jdplus.toolkit.base.api.timeseries.TsProvider;
import jdplus.toolkit.base.tsp.*;
import jdplus.toolkit.base.tsp.grid.GridReader;
import jdplus.toolkit.base.tsp.stream.HasTsStream;
import jdplus.toolkit.base.tsp.stream.TsStreamAsProvider;
import jdplus.toolkit.base.tsp.util.FallbackDataMoniker;
import jdplus.toolkit.base.tsp.util.ResourcePool;
import jdplus.toolkit.base.tsp.util.ShortLivedCachingLoader;
import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;

import java.io.File;
import java.io.IOException;

/**
 * @author Philippe Charles
 */
@DirectImpl
@ServiceProvider(TsProvider.class)
public final class SpreadSheetProvider implements FileLoader<SpreadSheetBean> {

    public static final String NAME = "XCLPRVDR";

    private final SpreadsheetManager spreadsheetManager;

    @lombok.experimental.Delegate
    private final HasDataSourceMutableList mutableListSupport;

    @lombok.experimental.Delegate
    private final HasDataMoniker monikerSupport;

    @lombok.experimental.Delegate
    private final HasDataSourceBean<SpreadSheetBean> beanSupport;

    @lombok.experimental.Delegate
    private final HasFilePaths filePathSupport;

    @lombok.experimental.Delegate
    private final HasDataDisplayName displayNameSupport;

    @lombok.experimental.Delegate(excludes = HasTsStream.class)
    private final SpreadSheetSupport spreadSheetSupport;

    @lombok.experimental.Delegate
    private final TsProvider tsSupport;

    public SpreadSheetProvider() {
        this.spreadsheetManager = SpreadsheetManager.ofServiceLoader();

        ResourcePool<SpreadSheetConnection> pool = SpreadSheetSupport.newConnectionPool();
        SpreadSheetParam param = new SpreadSheetParam.V1();

        this.mutableListSupport = HasDataSourceMutableList.of(NAME, pool::remove);
        this.monikerSupport = FallbackDataMoniker.of(HasDataMoniker.usingUri(NAME), LegacySpreadSheetMoniker.of(NAME, param));
        this.beanSupport = HasDataSourceBean.of(NAME, param, param.getVersion());
        this.filePathSupport = HasFilePaths.of(pool::clear);
        this.displayNameSupport = SpreadSheetDataDisplayName.of(NAME, param);
        this.spreadSheetSupport = SpreadSheetSupport.of(NAME, pool.asFactory(dataSource -> openConnection(dataSource, filePathSupport, param, spreadsheetManager)), ignore -> param.getSheetParam(), ignore -> param.getSeriesParam());
        this.tsSupport = TsStreamAsProvider.of(NAME, spreadSheetSupport, monikerSupport, pool::clear);
    }

    @Override
    public @NonNull String getDisplayName() {
        return "Spreadsheets";
    }

    @Override
    public @NonNull String getFileDescription() {
        return "Spreadsheet file";
    }

    @Override
    public boolean accept(File pathname) {
        return spreadsheetManager.getReader(pathname).isPresent();
    }

    private static SpreadSheetConnection openConnection(DataSource key, HasFilePaths paths, SpreadSheetParam param, SpreadsheetManager books) throws IOException {
        SpreadSheetBean bean = param.get(key);
        File file = paths.resolveFilePath(bean.getFile());
        Book.Factory factory = books.getReader(file).orElseThrow(() -> new IOException("File type not supported"));
        SheetGrid result = SheetGrid.of(file, factory, getReader(bean));
        return CachedSpreadSheetConnection.of(result, file, ShortLivedCachingLoader.get());
    }

    private static GridReader getReader(SpreadSheetBean bean) {
        return GridReader
                .builder()
                .format(bean.getFormat())
                .gathering(bean.getGathering())
                .build();
    }
}
