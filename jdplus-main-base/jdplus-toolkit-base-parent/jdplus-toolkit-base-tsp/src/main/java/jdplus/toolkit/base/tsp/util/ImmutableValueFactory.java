package jdplus.toolkit.base.tsp.util;

import jdplus.toolkit.base.tsp.DataSource;
import lombok.NonNull;

import java.io.IOException;

@FunctionalInterface
public interface ImmutableValueFactory<VALUE> {

    @NonNull VALUE load(@NonNull DataSource dataSource) throws IOException;
}
