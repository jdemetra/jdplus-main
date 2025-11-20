import jdplus.toolkit.base.workspace.file.spi.FamilyHandler;
import jdplus.tramoseats.base.workspace.TramoSeatsHandlers;

module jdplus.tramoseats.base.workspace {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires transitive jdplus.tramoseats.base.api;
    requires jdplus.toolkit.base.information;
    requires jdplus.tramoseats.base.information;
    requires jdplus.toolkit.base.workspace;
    requires jdplus.tramoseats.base.core;

    exports jdplus.tramoseats.base.workspace;

    provides FamilyHandler with
            TramoSeatsHandlers.ModDocTramoLegacy,
            TramoSeatsHandlers.SaDocTramoSeats,
            TramoSeatsHandlers.ModSpecTramo,
            TramoSeatsHandlers.SaSpecTramoseatsLegacy,
            TramoSeatsHandlers.ModSpecTramoLegacy,
            TramoSeatsHandlers.SaSpecTramoseats,
            TramoSeatsHandlers.ModDocTramo,
            TramoSeatsHandlers.SaDocTramoSeatsLegacy;
}