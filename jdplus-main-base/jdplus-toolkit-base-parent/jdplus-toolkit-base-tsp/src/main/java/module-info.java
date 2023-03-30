module jdplus.toolkit.base.tsp {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires nbbrd.io.base;
    requires transitive jdplus.toolkit.base.api;
    requires java.desktop;
    requires java.logging;

    exports jdplus.toolkit.base.tsp;
    exports jdplus.toolkit.base.tsp.cube;
    exports jdplus.toolkit.base.tsp.grid;
    exports jdplus.toolkit.base.tsp.legacy;
    exports jdplus.toolkit.base.tsp.stream;
    exports jdplus.toolkit.base.tsp.util;

    // FIXME:
    exports jdplus.toolkit.base.tsp.fixme;

    uses jdplus.toolkit.base.tsp.util.IOCacheFactory;

    provides jdplus.toolkit.base.tsp.util.IOCacheFactory with
            internal.toolkit.base.tsp.util.DefaultIOCacheFactory;
}