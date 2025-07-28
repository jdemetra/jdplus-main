module jdplus.tramoseats.base.protobuf {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires transitive jdplus.tramoseats.base.api;
    requires jdplus.tramoseats.base.core;
    requires jdplus.toolkit.base.protobuf;
    requires jdplus.sa.base.protobuf;
    requires jdplus.toolkit.base.core;

    exports jdplus.tramoseats.base.protobuf;
}