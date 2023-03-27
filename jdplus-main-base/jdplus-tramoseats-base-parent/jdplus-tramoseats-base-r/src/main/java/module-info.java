module jdplus.tramoseats.base.r {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive jdplus.tramoseats.base.api;
    requires com.google.protobuf;
    requires jdplus.tramoseats.base.protobuf;
    requires jdplus.tramoseats.base.core;
    requires jdplus.toolkit.base.core;
    requires jdplus.toolkit.base.protobuf;

    exports demetra.tramoseats.r;
}