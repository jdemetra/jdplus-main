module jdplus.toolkit.base.information {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires transitive jdplus.toolkit.base.api;

    exports jdplus.toolkit.base.information;
}