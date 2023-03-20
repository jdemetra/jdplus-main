module jdplus.x13.base.workspace {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive jdplus.x13.base.api;
    requires jdplus.toolkit.base.information;
    requires jdplus.toolkit.base.workspace;
    requires jdplus.x13.base.information;
    requires jdplus.x13.base.core;

    exports demetra.x13.workspace;

    provides demetra.workspace.file.spi.FamilyHandler with
            demetra.x13.workspace.X13Handlers.ModSpecRegarima,
            demetra.x13.workspace.X13Handlers.ModDocRegarimaLegacy,
            demetra.x13.workspace.X13Handlers.SaSpecX13,
            demetra.x13.workspace.X13Handlers.SaDocX13,
            demetra.x13.workspace.X13Handlers.SaSpecX13Legacy,
            demetra.x13.workspace.X13Handlers.ModDocRegarima;
}