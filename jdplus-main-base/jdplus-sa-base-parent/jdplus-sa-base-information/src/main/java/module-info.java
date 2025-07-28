import jdplus.sa.base.information.SaSpecificationMapping;

module jdplus.sa.base.information {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires transitive jdplus.sa.base.api;
    requires jdplus.toolkit.base.information;

    exports jdplus.sa.base.information;
    exports jdplus.sa.base.information.highfreq;

    uses SaSpecificationMapping;
}