import jdplus.sa.base.information.SaSpecificationMapping;
import jdplus.x13.base.information.X13SpecMapping;

module jdplus.x13.base.information {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires transitive jdplus.x13.base.api;
    requires jdplus.toolkit.base.information;
    requires jdplus.sa.base.information;

    exports jdplus.x13.base.information;

    provides SaSpecificationMapping with
            X13SpecMapping.Serializer;
}