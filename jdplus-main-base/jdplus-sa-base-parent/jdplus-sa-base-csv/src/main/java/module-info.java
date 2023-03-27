module jdplus.sa.base.csv {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive jdplus.sa.base.api;

    exports demetra.sa.csv;

    provides demetra.sa.SaOutputFactory with
            demetra.sa.csv.CsvArrayOutputFactory,
            demetra.sa.csv.CsvMatrixOutputFactory,
            demetra.sa.csv.CsvOutputFactory;
}