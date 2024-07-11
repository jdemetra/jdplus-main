/*
 * Copyright 2018 National Bank of Belgium
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

import _test.MyDbConnectionManager;
import jdplus.sql.base.api.jdbc.JdbcBean;
import jdplus.sql.base.api.jdbc.JdbcProvider;
import jdplus.toolkit.base.api.timeseries.TsDataTable;
import jdplus.toolkit.base.tsp.DataSource;
import jdplus.toolkit.base.tsp.cube.TableAsCube;
import jdplus.toolkit.base.tspbridge.demo.ProviderDemo;

import java.io.IOException;

/**
 * @author Philippe Charles
 */
public class JdbcProviderDemo {

    public static void main(String[] args) throws IOException {
        // 1. create and configure the provider
        try (JdbcProvider provider = new JdbcProvider()) {
            provider.setConnectionManager(new MyDbConnectionManager());

            // 2. create and configure a bean
            JdbcBean bean = provider.newBean();
            bean.setDatabase("mydb");
            bean.setTable("Table2");
            bean.setCube(TableAsCube
                    .builder()
                    .dimension("Sector")
                    .dimension("Region")
                    .timeDimension("Period")
                    .measure("Rate")
                    .build());

            // 3. create and open a DataSource from the bean
            DataSource dataSource = provider.encodeBean(bean);
            provider.open(dataSource);

            // 4. run demos
            ProviderDemo.printTree(provider, dataSource);
            ProviderDemo.printFirstSeries(provider, dataSource);
            ProviderDemo.printDataTable(provider, dataSource, TsDataTable.DistributionType.FIRST);

            // 5. close resources
            provider.close(dataSource);
        }
    }
}
