module jdplus.toolkit.base.protobuf {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires jdplus.toolkit.base.core;
    requires protobuf.java;

    exports jdplus.toolkit.base.protobuf.modelling;
    exports jdplus.toolkit.base.protobuf.regarima;
    exports jdplus.toolkit.base.protobuf.toolkit;
    exports jdplus.toolkit.base.protobuf.outliers;
}