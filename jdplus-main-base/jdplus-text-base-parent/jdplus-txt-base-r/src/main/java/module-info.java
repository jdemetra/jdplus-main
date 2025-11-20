
module jdplus.text.base.r {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires jdplus.toolkit.base.api;
    requires jdplus.toolkit.base.core;
    requires jdplus.toolkit.base.tsp;
    requires jdplus.text.base.api;
   requires jdplus.toolkit.base.r;

    exports jdplus.text.base.r;
}