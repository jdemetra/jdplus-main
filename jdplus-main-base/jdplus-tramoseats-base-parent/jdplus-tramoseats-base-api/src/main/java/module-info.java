import jdplus.tramoseats.base.api.seats.Seats;
import jdplus.tramoseats.base.api.tramo.Tramo;
import jdplus.tramoseats.base.api.tramoseats.TramoSeats;

module jdplus.tramoseats.base.api {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires transitive jdplus.sa.base.api;

    exports jdplus.tramoseats.base.api.seats;
    exports jdplus.tramoseats.base.api.tramo;
    exports jdplus.tramoseats.base.api.tramoseats;

    uses TramoSeats.Processor;
    uses Tramo.Processor;
    uses Seats.Processor;
}