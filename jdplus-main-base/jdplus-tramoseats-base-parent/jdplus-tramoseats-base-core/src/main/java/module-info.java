module jdplus.tramoseats.base.core {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive jdplus.tramoseats.base.api;
    requires jdplus.toolkit.base.core;
    requires jdplus.sa.base.core;

    exports jdplus.seats;
    exports jdplus.seats.diagnostics;
    exports jdplus.tramo;
    exports jdplus.tramo.internal;
    exports jdplus.tramo.spi;
    exports jdplus.tramoseats;
    exports jdplus.tramoseats.extractors;
    exports jdplus.tramoseats.spi;

    provides demetra.sa.SaProcessingFactory with
            jdplus.tramoseats.TramoSeatsFactory;

    provides demetra.information.InformationExtractor with
            jdplus.tramoseats.extractors.SeatsDiagnosticsExtractor,
            jdplus.tramoseats.extractors.TramoSeatsDiagnosticsExtractor,
            jdplus.tramoseats.extractors.TramoSeatsExtractor,
            jdplus.tramoseats.extractors.SeatsExtractor;

    provides demetra.tramoseats.TramoSeats.Processor with
            jdplus.tramoseats.spi.TramoSeatsProcessor;

    provides demetra.seats.Seats.Processor with
            jdplus.tramoseats.spi.SeatsProcessor;

    provides demetra.tramo.Tramo.Processor with
            jdplus.tramo.spi.TramoComputer;
}