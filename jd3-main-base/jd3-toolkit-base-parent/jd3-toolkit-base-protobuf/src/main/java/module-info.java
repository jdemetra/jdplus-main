@SuppressWarnings("JavaModuleNaming")
module jd3.toolkit.base.protobuf {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires jd3.toolkit.base.api;
    requires jd3.toolkit.base.core;
    requires com.google.protobuf;

    exports demetra.modelling.io.protobuf;
    exports demetra.outliers.io.protobuf;
    exports demetra.regarima.io.protobuf;
    exports demetra.toolkit.io.protobuf;
}