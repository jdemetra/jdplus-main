import jdplus.sa.base.information.SaSpecificationMapping;

module jdplus.sa.base.information {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive jdplus.sa.base.api;
    requires jdplus.toolkit.base.information;

    exports jdplus.sa.base.information;

    uses SaSpecificationMapping;
}