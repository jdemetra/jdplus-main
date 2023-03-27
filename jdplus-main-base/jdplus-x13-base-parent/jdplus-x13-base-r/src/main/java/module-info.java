module jdplus.x13.base.r {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive jdplus.x13.base.api;
    requires jdplus.toolkit.base.protobuf;
    requires jdplus.x13.base.protobuf;
    requires jdplus.toolkit.base.core;
    requires jdplus.x13.base.core;

    exports demetra.x13.r;
}