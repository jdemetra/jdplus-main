package jdplus.toolkit.base.tspbridge;

import jdplus.toolkit.base.tsp.DataSource;
import jdplus.toolkit.base.tsp.FileBean;
import jdplus.toolkit.base.tsp.FileLoader;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;
import java.util.Objects;

public class ToFileLoader extends ToDataSourceLoader implements FileLoader {

    public static @NonNull FileLoader<? extends FileBean> toFileLoader(ec.tss.tsproviders.@NonNull IFileLoader delegate) {
        return delegate instanceof FromFileLoader
                ? ((FromFileLoader) delegate).getDelegate()
                : new ToFileLoader(delegate);
    }

    protected ToFileLoader(ec.tss.tsproviders.IFileLoader delegate) {
        super(delegate);
    }

    @lombok.NonNull
    @Override
    public ec.tss.tsproviders.IFileLoader getDelegate() {
        return (ec.tss.tsproviders.IFileLoader) super.getDelegate();
    }

    @Override
    public @NonNull FileBean newBean() {
        return ToFileBean.toFileBean(getDelegate().newBean());
    }

    @Override
    public @NonNull DataSource encodeBean(@NonNull Object bean) throws IllegalArgumentException {
        Objects.requireNonNull(bean);
        if (!(bean instanceof FileBean)) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "Invalid bean type: expected='%s', found='%s'", FileBean.class, bean.getClass()));
        }
        return TsConverter.toDataSource(getDelegate().encodeBean(FromFileBean.fromFileBean((FileBean) bean)));
    }

    @Override
    public @NonNull FileBean decodeBean(@NonNull DataSource dataSource) throws IllegalArgumentException {
        return ToFileBean.toFileBean(getDelegate().decodeBean(TsConverter.fromDataSource(dataSource)));
    }

    @Override
    public @NonNull String getFileDescription() {
        return getDelegate().getFileDescription();
    }

    @Override
    public void setPaths(@Nullable File[] paths) {
        getDelegate().setPaths(paths);
    }

    @Override
    public @NonNull File[] getPaths() {
        return getDelegate().getPaths();
    }

    @Override
    public @NonNull File resolveFilePath(@NonNull File file) throws FileNotFoundException {
        return getDelegate().resolveFilePath(file);
    }

    @Override
    public boolean accept(File pathname) {
        return getDelegate().accept(pathname);
    }
}
