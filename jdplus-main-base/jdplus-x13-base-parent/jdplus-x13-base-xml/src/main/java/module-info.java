module jdplus.x13.base.xml {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires transitive jdplus.x13.base.api;
    requires java.xml.bind;
    requires jdplus.toolkit.base.xml;
    requires jdplus.sa.base.xml;

    exports jdplus.x13.base.xml;
}