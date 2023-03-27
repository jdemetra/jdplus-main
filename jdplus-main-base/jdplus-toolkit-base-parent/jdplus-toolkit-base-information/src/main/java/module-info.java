module jdplus.toolkit.base.information {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive jdplus.toolkit.base.api;

    exports demetra.modelling.io.information;
}