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
package _test;

import ec.tss.tsproviders.jdbc.jndi.JndiJdbcProvider;
import jdplus.sql.base.api.jdbc.JdbcBean;
import jdplus.sql.base.api.jdbc.JdbcProvider;
import jdplus.toolkit.base.tsp.cube.TableAsCube;
import jdplus.toolkit.base.tspbridge.demo.ProviderResources;

import java.sql.DriverManager;

/**
 * @author Philippe Charles
 */
public enum JdbcSamples implements ProviderResources.Loader2<JndiJdbcProvider>, ProviderResources.Loader3<JdbcProvider> {
    TABLE2;

    @Override
    public JndiJdbcProvider getProvider2() {
        JndiJdbcProvider provider = new JndiJdbcProvider();
        provider.setConnectionSupplier((ec.tss.tsproviders.jdbc.JdbcBean o) -> DriverManager.getConnection("jdbc:hsqldb:res:mydb", "sa", ""));
        return provider;
    }

    @Override
    public ec.tss.tsproviders.jdbc.JdbcBean getBean2(JndiJdbcProvider provider) {
        ec.tss.tsproviders.jdbc.JdbcBean bean = provider.newBean();
        bean.setDbName("mydb");
        bean.setTableName("Table2");
        bean.setDimColumns("Sector, Region");
        bean.setPeriodColumn("Period");
        bean.setValueColumn("Rate");
        return bean;
    }

    @Override
    public JdbcProvider getProvider3() {
        JdbcProvider provider = new JdbcProvider();
        provider.setConnectionManager(new MyDbConnectionManager());
        return provider;
    }

    @Override
    public JdbcBean getBean3(JdbcProvider provider) {
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
        return bean;
    }
}
