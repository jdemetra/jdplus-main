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
package _demo;

import jdplus.toolkit.base.tspbridge.demo.ProviderDemo;
import jdplus.spreadsheet.base.api.SpreadSheetBean;
import jdplus.spreadsheet.base.api.SpreadSheetProvider;
import jdplus.toolkit.base.api.timeseries.TsDataTable;
import jdplus.toolkit.base.tsp.DataSource;
import _test.Top5Browsers;
import nbbrd.design.Demo;

import java.io.File;
import java.io.IOException;
import java.util.List;
import jdplus.toolkit.base.api.timeseries.TsMoniker;
import jdplus.toolkit.base.tsp.DataSet;

/**
 * @author Philippe Charles
 */
public class SpreadSheetProviderDemo {

    @Demo
    public static void main(String[] args) throws IOException {
        File file = Top5Browsers.getRefFile();
        // 1. create and configure the provider
        try (SpreadSheetProvider provider = new SpreadSheetProvider()) {

            // 2. create and configure a bean
            SpreadSheetBean bean = provider.newBean();
            bean.setFile(file);
            
 
            // 3. create and open a DataSource from the bean
            DataSource dataSource = provider.encodeBean(bean);
            List<DataSet> children = provider.children(dataSource);
            
            provider.open(dataSource);
            TsMoniker toMoniker = provider.toMoniker(dataSource);

            // 4. run demos
            ProviderDemo.printTree(provider, dataSource);
            ProviderDemo.printFirstSeries(provider, dataSource);
            ProviderDemo.printDataTable(provider, dataSource, TsDataTable.DistributionType.FIRST);

            // 5. close resources
            provider.close(dataSource);
        }
    }
}
