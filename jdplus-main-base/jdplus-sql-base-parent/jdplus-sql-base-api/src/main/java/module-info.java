import jdplus.sql.base.api.jdbc.JdbcProvider;
import jdplus.sql.base.api.odbc.OdbcProvider;
import jdplus.toolkit.base.api.timeseries.TsProvider;

module jdplus.sql.base.api {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires jdplus.toolkit.base.tsp;
    requires nbbrd.sql.jdbc;
    requires nbbrd.sql.odbc;

    exports jdplus.sql.base.api;
    exports jdplus.sql.base.api.jdbc;
    exports jdplus.sql.base.api.odbc;

    provides TsProvider with
            JdbcProvider,
            OdbcProvider;
}