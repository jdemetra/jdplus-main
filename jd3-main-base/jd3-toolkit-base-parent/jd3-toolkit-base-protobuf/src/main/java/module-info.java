module jdplus.toolkit.base.protobuf {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires jdplus.toolkit.base.core;
    requires transitive com.google.protobuf;

    exports demetra.modelling.io.protobuf;
    exports demetra.outliers.io.protobuf;
    exports demetra.regarima.io.protobuf;
    exports demetra.toolkit.io.protobuf;
}