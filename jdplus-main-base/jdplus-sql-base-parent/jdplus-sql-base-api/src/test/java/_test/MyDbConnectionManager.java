package _test;

import internal.sql.base.api.DefaultConnectionSource;
import jdplus.sql.base.api.ConnectionManager;
import jdplus.sql.base.api.ConnectionSource;
import lombok.NonNull;

import java.sql.DriverManager;

public final class MyDbConnectionManager implements ConnectionManager {

    @Override
    public @NonNull String getId() {
        return "mydb";
    }

    @Override
    public @NonNull ConnectionSource getSource(@NonNull String connectionString) {
        return new DefaultConnectionSource(o -> DriverManager.getConnection("jdbc:hsqldb:res:" + o, "sa", ""), connectionString);
    }
}
