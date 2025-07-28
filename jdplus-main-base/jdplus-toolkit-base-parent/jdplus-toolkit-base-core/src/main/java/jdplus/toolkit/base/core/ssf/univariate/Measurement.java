/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.ssf.univariate;

import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.basic.MeasurementError;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;

/**
 *
 * @author palatej
 */
public class Measurement implements ISsfMeasurement {

    private final ISsfLoading loading;
    private final ISsfError error;

    public Measurement(@NonNull final ISsfLoading loading, @Nullable final ISsfError error) {
        this.loading = loading;
        this.error = error;
    }

    public Measurement(@NonNull final ISsfLoading loading, final double var) {
        this.loading = loading;
        this.error = MeasurementError.of(var);
    }

    @Override
    public ISsfLoading loading() {
        return loading;
    }

    @Override
    public ISsfError error() {
        return error;
    }

    @Override
    public boolean hasError() {
        return error != null;
    }

}
