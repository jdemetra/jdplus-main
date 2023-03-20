module jdplus.tramoseats.base.protobuf {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive jdplus.tramoseats.base.api;
    requires jdplus.tramoseats.base.core;
    requires jdplus.toolkit.base.protobuf;
    requires jdplus.sa.base.protobuf;
    requires jdplus.toolkit.base.core;

    exports demetra.tramoseats.io.protobuf;
}