import jdplus.sa.base.api.SaOutputFactory;
import jdplus.sa.base.csv.CsvArrayOutputFactory;
import jdplus.sa.base.csv.CsvMatrixOutputFactory;
import jdplus.sa.base.csv.CsvOutputFactory;

module jdplus.sa.base.csv {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires transitive jdplus.sa.base.api;
    requires nbbrd.io.picocsv;

    exports jdplus.sa.base.csv;

    provides SaOutputFactory with
            CsvArrayOutputFactory,
            CsvMatrixOutputFactory,
            CsvOutputFactory;
}