package jdplus.sql.base.api;

import lombok.NonNull;
import nbbrd.design.ThreadSafe;

@ThreadSafe
public interface ConnectionManager {

    @NonNull
    String getId();

    @NonNull
    ConnectionSource getSource(@NonNull String connectionString);
}
