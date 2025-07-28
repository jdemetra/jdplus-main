module jdplus.sa.base.r {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires jdplus.sa.base.core;
    requires jdplus.sa.base.protobuf;
    requires jdplus.toolkit.base.core;

    exports jdplus.sa.base.r;
}