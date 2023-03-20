module jdplus.sql.base.api {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires jdplus.toolkit.base.tsp;
    requires nbbrd.sql.jdbc;
    requires nbbrd.sql.odbc;

    exports demetra.sql;
    exports demetra.sql.jdbc;
    exports demetra.sql.odbc;

    provides demetra.timeseries.TsProvider with
            demetra.sql.jdbc.JdbcProvider,
            demetra.sql.odbc.OdbcProvider;
}