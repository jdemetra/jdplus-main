module jdplus.tramoseats.base.information {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive jdplus.tramoseats.base.api;
    requires jdplus.toolkit.base.information;
    requires jdplus.sa.base.information;

    exports demetra.tramoseats.io.information;

    provides demetra.sa.io.information.SaSpecificationMapping with
            demetra.tramoseats.io.information.TramoSeatsSpecMapping.Serializer;
}