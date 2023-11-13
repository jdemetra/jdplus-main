package jdplus.toolkit.desktop.plugin.components.parts;

import jdplus.toolkit.desktop.plugin.DemetraUI;
import jdplus.toolkit.base.tsp.util.ObsFormat;
import lombok.NonNull;

import java.beans.PropertyChangeEvent;

public final class HasObsFormatResolver {

    @lombok.NonNull
    private final HasObsFormat property;

    @lombok.NonNull
    private final Runnable onChange;

    public HasObsFormatResolver(HasObsFormat property, Runnable onChange) {
        this.property = property;
        this.onChange = onChange;
        DemetraUI.get().addWeakPropertyChangeListener(this::onPropertyChange);
    }

    @NonNull
    public ObsFormat resolve() {
        ObsFormat result = property.getObsFormat();
        return result != null ? result : DemetraUI.get().getObsFormat();
    }

    private void onPropertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(DemetraUI.OBS_FORMAT_PROPERTY) && !property.hasObsFormat()) {
            onChange.run();
        }
    }
}
