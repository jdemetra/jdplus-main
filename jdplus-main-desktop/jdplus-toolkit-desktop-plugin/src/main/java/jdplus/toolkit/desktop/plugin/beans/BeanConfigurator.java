package jdplus.toolkit.desktop.plugin.beans;

import jdplus.toolkit.desktop.plugin.Config;
import jdplus.toolkit.desktop.plugin.ConfigEditor;
import jdplus.toolkit.desktop.plugin.Converter;
import lombok.NonNull;
import org.openide.util.Exceptions;

@lombok.AllArgsConstructor
public final class BeanConfigurator<B, R> implements ConfigEditor {

    @lombok.NonNull
    private final BeanHandler<B, R> beanHandler;

    @lombok.NonNull
    private final Converter<B, Config> converter;

    @lombok.NonNull
    private final BeanEditor editor;

    @NonNull
    public Config getConfig(R resource) {
        return converter.doForward(beanHandler.load(resource));
    }

    public void setConfig(@NonNull R resource, @NonNull Config config) throws IllegalArgumentException {
        beanHandler.store(resource, converter.doBackward(config));
    }

    @Override
    public @NonNull Config editConfig(@NonNull Config config) throws IllegalArgumentException {
        B bean = converter.doBackward(config);
        if (editor.editBean(bean, Exceptions::printStackTrace)) {
            return converter.doForward(bean);
        }
        return config;
    }
}
