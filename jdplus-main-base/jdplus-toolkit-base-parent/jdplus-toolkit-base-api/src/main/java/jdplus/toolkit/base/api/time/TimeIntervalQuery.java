package jdplus.toolkit.base.api.time;

@FunctionalInterface
public interface TimeIntervalQuery<R> {

    R queryFrom(TimeIntervalAccessor timeInterval);
}
