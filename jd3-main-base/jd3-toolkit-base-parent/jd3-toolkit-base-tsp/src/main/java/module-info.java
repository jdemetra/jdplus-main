module jdplus.toolkit.base.tsp {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires nbbrd.io.base;
    requires transitive jdplus.toolkit.base.api;
    requires java.desktop;
    requires java.logging;

    exports demetra.io;
    exports demetra.tsprovider;
    exports demetra.tsprovider.cube;
    exports demetra.tsprovider.grid;
    exports demetra.tsprovider.legacy;
    exports demetra.tsprovider.stream;
    exports demetra.tsprovider.util;
    exports demetra.util2;

    // FIXME:
    exports internal.util to jdplus.sql.base.api, jdplus.text.base.api;

    uses demetra.tsprovider.util.IOCacheFactory;

    provides demetra.tsprovider.util.IOCacheFactory with
            internal.tsprovider.util.DefaultIOCacheFactory;
}