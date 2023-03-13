@SuppressWarnings("JavaModuleNaming")
module jd3.toolkit.base.tsp {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires nbbrd.io.base;
    requires jd3.toolkit.base.api;
    requires java.desktop;
    requires java.logging;

    uses demetra.tsprovider.util.IOCacheFactory;

    provides demetra.tsprovider.util.IOCacheFactory with
            internal.tsprovider.util.DefaultIOCacheFactory;
}