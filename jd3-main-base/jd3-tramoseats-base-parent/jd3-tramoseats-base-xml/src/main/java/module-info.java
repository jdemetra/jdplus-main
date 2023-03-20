module jdplus.tramoseats.base.xml {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive jdplus.tramoseats.base.api;
    requires java.xml.bind;
    requires jdplus.toolkit.base.xml;
    requires jdplus.sa.base.xml;

    exports demetra.tramoseats.io.xml.legacy;
}