module jdplus.sa.base.xml {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires nbbrd.io.xml.bind;
    requires jdplus.sa.base.api;
    requires jdplus.toolkit.base.xml;

    exports demetra.sa.io.xml.legacy;
    exports demetra.sa.io.xml.legacy.benchmarking;
}