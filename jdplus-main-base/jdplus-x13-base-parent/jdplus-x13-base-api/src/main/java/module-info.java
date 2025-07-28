import jdplus.x13.base.api.regarima.RegArima;
import jdplus.x13.base.api.x11.X11;
import jdplus.x13.base.api.x13.X13;

module jdplus.x13.base.api {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires transitive jdplus.sa.base.api;

    exports jdplus.x13.base.api.regarima;
    exports jdplus.x13.base.api.x11;
    exports jdplus.x13.base.api.x13;

    uses X11.Processor;
    uses RegArima.Processor;
    uses X13.Processor;
}