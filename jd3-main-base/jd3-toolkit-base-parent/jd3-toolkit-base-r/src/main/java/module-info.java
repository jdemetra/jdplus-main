@SuppressWarnings("JavaModuleNaming")
module jd3.toolkit.base.r {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires jd3.toolkit.base.api;
    requires jd3.toolkit.base.core;
    requires jd3.toolkit.base.protobuf;
    requires com.google.protobuf;

    provides demetra.information.InformationExtractor with
            demetra.arima.r.extensions.SarimaExtension;
}