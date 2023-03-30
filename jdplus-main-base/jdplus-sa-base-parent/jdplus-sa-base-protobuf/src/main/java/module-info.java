module jdplus.sa.base.protobuf {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires jdplus.sa.base.core;
    requires jdplus.toolkit.base.protobuf;

    exports jdplus.sa.base.protobuf;
}