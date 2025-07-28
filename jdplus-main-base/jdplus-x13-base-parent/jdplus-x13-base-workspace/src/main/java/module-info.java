import jdplus.toolkit.base.workspace.file.spi.FamilyHandler;
import jdplus.x13.base.workspace.X13Handlers;

module jdplus.x13.base.workspace {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires transitive jdplus.x13.base.api;
    requires jdplus.toolkit.base.information;
    requires jdplus.toolkit.base.workspace;
    requires jdplus.x13.base.information;
    requires jdplus.x13.base.core;

    exports jdplus.x13.base.workspace;

    provides FamilyHandler with
            X13Handlers.ModSpecRegarima,
            X13Handlers.ModDocRegarimaLegacy,
            X13Handlers.SaSpecX13,
            X13Handlers.SaDocX13,
            X13Handlers.SaSpecX13Legacy,
            X13Handlers.ModDocRegarima;
}