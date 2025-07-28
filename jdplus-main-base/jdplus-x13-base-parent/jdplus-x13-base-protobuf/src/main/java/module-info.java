module jdplus.x13.base.protobuf {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires transitive jdplus.x13.base.api;
    requires jdplus.x13.base.core;
    requires jdplus.sa.base.protobuf;
    requires jdplus.toolkit.base.protobuf;

    exports jdplus.x13.base.protobuf;
}