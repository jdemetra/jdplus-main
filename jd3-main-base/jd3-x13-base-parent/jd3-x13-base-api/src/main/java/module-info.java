module jdplus.x13.base.api {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive jdplus.sa.base.api;

    exports demetra.regarima;
    exports demetra.x11;
    exports demetra.x13;

    uses demetra.x11.X11.Processor;
    uses demetra.regarima.RegArima.Processor;
    uses demetra.x13.X13.Processor;
}