package jdplus.sql.base.api;

import lombok.NonNull;
import nbbrd.design.ThreadSafe;

import java.sql.Connection;
import java.sql.SQLException;

@ThreadSafe
public interface ConnectionSource {

    @NonNull
    String getId();

    @NonNull
    Connection open() throws SQLException;
}
