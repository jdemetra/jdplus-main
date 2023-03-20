module jdplus.text.base.api {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires jdplus.toolkit.base.tsp;
    requires nbbrd.io.base;
    requires nbbrd.io.picocsv;
    requires nbbrd.io.xml;

    exports demetra.tsp.text;

    provides demetra.timeseries.TsProvider with
            demetra.tsp.text.TxtProvider,
            demetra.tsp.text.XmlProvider;
}