import jdplus.sa.base.information.SaSpecificationMapping;
import jdplus.tramoseats.base.information.TramoSeatsSpecMapping;

module jdplus.tramoseats.base.information {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires transitive jdplus.tramoseats.base.api;
    requires jdplus.toolkit.base.information;
    requires jdplus.sa.base.information;

    exports jdplus.tramoseats.base.information;

    provides SaSpecificationMapping with
            TramoSeatsSpecMapping.Serializer;
}