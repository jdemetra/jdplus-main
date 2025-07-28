import jdplus.text.base.api.TxtProvider;
import jdplus.text.base.api.XmlProvider;
import jdplus.toolkit.base.api.timeseries.TsProvider;

module jdplus.text.base.api {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires jdplus.toolkit.base.tsp;
    requires nbbrd.io.base;
    requires nbbrd.io.picocsv;
    requires nbbrd.io.xml;

    exports jdplus.text.base.api;

    provides TsProvider with
            TxtProvider,
            XmlProvider;
}