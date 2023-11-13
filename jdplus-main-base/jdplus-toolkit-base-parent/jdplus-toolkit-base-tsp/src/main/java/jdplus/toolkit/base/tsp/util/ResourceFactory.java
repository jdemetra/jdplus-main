package jdplus.toolkit.base.tsp.util;

import jdplus.toolkit.base.tsp.DataSource;
import lombok.NonNull;

import java.io.Closeable;
import java.io.IOException;

@FunctionalInterface
public interface ResourceFactory<T extends Closeable> {

    @NonNull
    T open(@NonNull DataSource dataSource) throws IOException;
}
