module jdplus.sa.base.xml {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires nbbrd.io.xml.bind;
    requires jdplus.sa.base.api;
    requires jdplus.toolkit.base.xml;

    exports jdplus.sa.base.xml;
    exports jdplus.sa.base.xml.benchmarking;
}