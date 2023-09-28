package jdplus.toolkit.desktop.plugin.util;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.MightBePromoted;
import nbbrd.design.StaticFactoryMethod;

import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@lombok.RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Stopwatch {

    private final LongSupplier ticker = System::nanoTime;
    private boolean isRunning = false;
    private long elapsedNanos = 0;
    private long startTick = 0;

    @StaticFactoryMethod
    public static @NonNull Stopwatch createStarted() {
        return new Stopwatch().start();
    }

    public boolean isRunning() {
        return isRunning;
    }

    public @NonNull Stopwatch reset() {
        isRunning = false;
        elapsedNanos = 0L;
        return this;
    }

    public @NonNull Stopwatch start() {
        if (isRunning) {
            throw new IllegalStateException("This stopwatch is already running.");
        }
        isRunning = true;
        startTick = ticker.getAsLong();
        return this;
    }

    public @NonNull Stopwatch stop() {
        if (!isRunning) {
            throw new IllegalStateException("This stopwatch is already stopped.");
        }
        isRunning = false;
        elapsedNanos += ticker.getAsLong() - startTick;
        return this;
    }

    public long elapsed(@NonNull TimeUnit timeUnit) {
        return timeUnit.convert(elapsedNanos(), TimeUnit.NANOSECONDS);
    }

    @Override
    public String toString() {
        long nanos = elapsedNanos();
        TimeUnit unit = chooseUnit(nanos);
        double value = (double) nanos / (double) TimeUnit.NANOSECONDS.convert(1L, unit);
        return formatCompact4Digits(value) + " " + abbreviate(unit);
    }

    private long elapsedNanos() {
        return isRunning ? ticker.getAsLong() - startTick + elapsedNanos : elapsedNanos;
    }

    private static TimeUnit chooseUnit(long nanos) {
        return reverseStream(TimeUnit.values())
                .filter(unit -> unit.convert(nanos, TimeUnit.NANOSECONDS) > 0L)
                .findFirst()
                .orElse(TimeUnit.NANOSECONDS);
    }

    private static String abbreviate(TimeUnit unit) {
        return switch (unit) {
            case NANOSECONDS -> "ns";
            case MICROSECONDS -> "μs";
            case MILLISECONDS -> "ms";
            case SECONDS -> "s";
            case MINUTES -> "min";
            case HOURS -> "h";
            case DAYS -> "d";
        };
    }

    private static String formatCompact4Digits(double value) {
        return String.format(Locale.ROOT, "%.4g", value);
    }

    @MightBePromoted
    private static <T> Stream<T> reverseStream(T[] array) {
        return IntStream.rangeClosed(1, array.length)
                .mapToObj(i -> array[array.length - i]);
    }
}