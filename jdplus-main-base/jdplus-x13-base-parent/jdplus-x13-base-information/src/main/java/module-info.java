module jdplus.x13.base.information {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive jdplus.x13.base.api;
    requires jdplus.toolkit.base.information;
    requires jdplus.sa.base.information;

    exports demetra.x13.io.information;

    provides demetra.sa.io.information.SaSpecificationMapping with
            demetra.x13.io.information.X13SpecMapping.Serializer;
}