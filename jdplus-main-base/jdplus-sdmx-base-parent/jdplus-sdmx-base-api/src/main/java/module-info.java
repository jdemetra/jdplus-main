import jdplus.sdmx.base.api.file.SdmxFileProvider;
import jdplus.sdmx.base.api.web.SdmxWebProvider;
import jdplus.toolkit.base.api.timeseries.TsProvider;

module jdplus.sdmx.base.api {
    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;
    requires sdmxdl.api;
    requires jdplus.toolkit.base.tsp;
    requires nbbrd.io.base;

    exports jdplus.sdmx.base.api;
    exports jdplus.sdmx.base.api.file;
    exports jdplus.sdmx.base.api.web;

    provides TsProvider with
            SdmxFileProvider,
            SdmxWebProvider;
}
