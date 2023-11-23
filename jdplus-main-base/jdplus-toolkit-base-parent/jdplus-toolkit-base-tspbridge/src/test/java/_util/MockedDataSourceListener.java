package _util;

import jdplus.toolkit.base.tsp.DataSource;
import jdplus.toolkit.base.tsp.DataSourceListener;
import lombok.NonNull;

import java.util.List;

@lombok.AllArgsConstructor
public final class MockedDataSourceListener implements DataSourceListener {

    @lombok.NonNull
    private final List<Object> stack;

    @Override
    public void opened(@NonNull DataSource dataSource) {
        stack.add(dataSource);
    }

    @Override
    public void closed(@NonNull DataSource dataSource) {
        stack.add(dataSource);
    }

    @Override
    public void changed(@NonNull DataSource dataSource) {
        stack.add(dataSource);
    }

    @Override
    public void allClosed(@NonNull String providerName) {
        stack.add(providerName);
    }
}
