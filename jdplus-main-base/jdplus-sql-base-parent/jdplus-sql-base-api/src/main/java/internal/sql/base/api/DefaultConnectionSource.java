package internal.sql.base.api;

import jdplus.sql.base.api.ConnectionSource;
import lombok.NonNull;
import nbbrd.sql.jdbc.SqlConnectionSupplier;

import java.sql.Connection;
import java.sql.SQLException;

// TODO: use a connection pool
@lombok.Value
public class DefaultConnectionSource implements ConnectionSource {

    @NonNull
    SqlConnectionSupplier factory;

    @NonNull
    String connectionString;

    @Override
    public @NonNull String getId() {
        return connectionString;
    }

    @Override
    public @NonNull Connection open() throws SQLException {
        return factory.getConnection(connectionString);
    }
}
