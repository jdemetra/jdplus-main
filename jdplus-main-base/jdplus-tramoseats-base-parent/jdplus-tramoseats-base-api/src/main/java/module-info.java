module jdplus.tramoseats.base.api {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive jdplus.sa.base.api;

    exports demetra.seats;
    exports demetra.tramo;
    exports demetra.tramoseats;

    uses demetra.tramoseats.TramoSeats.Processor;
    uses demetra.tramo.Tramo.Processor;
    uses demetra.seats.Seats.Processor;
}