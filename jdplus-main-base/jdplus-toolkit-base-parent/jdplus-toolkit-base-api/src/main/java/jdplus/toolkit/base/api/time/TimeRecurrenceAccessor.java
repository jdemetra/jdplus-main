package jdplus.toolkit.base.api.time;

public interface TimeRecurrenceAccessor {

    TimeInterval<?, ?> getInterval();

    int length();
}
