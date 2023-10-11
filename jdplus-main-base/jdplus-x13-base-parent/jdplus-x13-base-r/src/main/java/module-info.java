module jdplus.x13.base.r {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires jdplus.toolkit.base.core;
    requires jdplus.toolkit.base.r;
    requires jdplus.toolkit.base.protobuf;
    requires transitive jdplus.x13.base.api;
    requires jdplus.x13.base.core;
    requires jdplus.x13.base.protobuf;
 
    exports jdplus.x13.base.r;
}
