package jdplus.toolkit.desktop.plugin.components;

import jdplus.toolkit.base.api.timeseries.Ts;
import jdplus.toolkit.base.api.timeseries.TsMoniker;
import lombok.NonNull;

@lombok.Value
public class TsIdentifier {

    @NonNull
    public static TsIdentifier of(@NonNull Ts ts) {
        return new TsIdentifier(ts.getName(), ts.getMoniker());
    }

    @lombok.NonNull
    String name;

    @lombok.NonNull
    TsMoniker moniker;
}
