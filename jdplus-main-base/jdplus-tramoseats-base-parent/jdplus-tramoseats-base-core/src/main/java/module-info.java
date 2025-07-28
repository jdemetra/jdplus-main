import jdplus.sa.base.api.SaProcessingFactory;
import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.tramoseats.base.api.seats.Seats;
import jdplus.tramoseats.base.api.tramo.Tramo;
import jdplus.tramoseats.base.api.tramoseats.TramoSeats;
import jdplus.tramoseats.base.core.tramo.spi.TramoComputer;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsFactory;

module jdplus.tramoseats.base.core {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires transitive jdplus.tramoseats.base.api;
    requires jdplus.toolkit.base.core;
    requires jdplus.sa.base.core;

    exports jdplus.tramoseats.base.core.seats;
    exports jdplus.tramoseats.base.core.seats.diagnostics;
    exports jdplus.tramoseats.base.core.tramo;
    exports jdplus.tramoseats.base.core.tramo.internal;
    exports jdplus.tramoseats.base.core.tramo.spi;
    exports jdplus.tramoseats.base.core.tramoseats.extractors;
    exports jdplus.tramoseats.base.core.tramoseats.spi;
    exports jdplus.tramoseats.base.core.tramoseats;

    provides SaProcessingFactory with
            TramoSeatsFactory;

    provides InformationExtractor with
            jdplus.tramoseats.base.core.tramoseats.extractors.SeatsDiagnosticsExtractor,
            jdplus.tramoseats.base.core.tramoseats.extractors.TramoSeatsDiagnosticsExtractor,
            jdplus.tramoseats.base.core.tramoseats.extractors.TramoSeatsExtractor,
            jdplus.tramoseats.base.core.tramoseats.extractors.SeatsExtractor;

    provides TramoSeats.Processor with
            jdplus.tramoseats.base.core.tramoseats.spi.TramoSeatsProcessor;

    provides Seats.Processor with
            jdplus.tramoseats.base.core.tramoseats.spi.SeatsProcessor;

    provides Tramo.Processor with
            TramoComputer;
}