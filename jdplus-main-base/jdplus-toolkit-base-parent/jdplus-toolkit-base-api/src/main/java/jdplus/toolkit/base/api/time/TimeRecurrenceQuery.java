package jdplus.toolkit.base.api.time;

@FunctionalInterface
public interface TimeRecurrenceQuery<R> {

    R queryFrom(TimeRecurrenceAccessor timeRecurrence);
}
