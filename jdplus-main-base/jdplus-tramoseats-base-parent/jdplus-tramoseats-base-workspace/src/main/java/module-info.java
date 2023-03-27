module jdplus.tramoseats.base.workspace {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive jdplus.tramoseats.base.api;
    requires jdplus.toolkit.base.information;
    requires jdplus.tramoseats.base.information;
    requires jdplus.toolkit.base.workspace;
    requires jdplus.tramoseats.base.core;

    exports demetra.tramoseats.workspace;

    provides demetra.workspace.file.spi.FamilyHandler with
            demetra.tramoseats.workspace.TramoSeatsHandlers.ModDocTramoLegacy,
            demetra.tramoseats.workspace.TramoSeatsHandlers.SaDocTramoSeats,
            demetra.tramoseats.workspace.TramoSeatsHandlers.ModSpecTramo,
            demetra.tramoseats.workspace.TramoSeatsHandlers.SaSpecTramoseatsLegacy,
            demetra.tramoseats.workspace.TramoSeatsHandlers.ModSpecTramoLegacy,
            demetra.tramoseats.workspace.TramoSeatsHandlers.SaSpecTramoseats,
            demetra.tramoseats.workspace.TramoSeatsHandlers.ModDocTramo,
            demetra.tramoseats.workspace.TramoSeatsHandlers.SaDocTramoSeatsLegacy;
}