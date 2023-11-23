package jdplus.toolkit.base.tsp.util;

import jdplus.toolkit.base.tsp.DataSet;
import lombok.NonNull;

import java.io.IOException;

@FunctionalInterface
public interface DataSetConversion<T, ID> {

    DataSet.@NonNull Converter<ID> getConverter(@NonNull T obj) throws IOException;
}
