module jdplus.tramoseats.base.r {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires jdplus.toolkit.base.core;
    requires jdplus.toolkit.base.protobuf;
    requires jdplus.toolkit.base.r;
    requires transitive jdplus.tramoseats.base.api;
    requires jdplus.tramoseats.base.core;
    requires jdplus.tramoseats.base.protobuf;
    requires protobuf.java;
 
    exports jdplus.tramoseats.base.r;
}