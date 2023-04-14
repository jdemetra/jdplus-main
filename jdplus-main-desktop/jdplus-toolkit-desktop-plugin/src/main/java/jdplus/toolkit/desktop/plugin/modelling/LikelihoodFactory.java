/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.toolkit.desktop.plugin.modelling;

import jdplus.toolkit.desktop.plugin.ui.processing.ProcDocumentItemFactory;
import jdplus.toolkit.desktop.plugin.ui.processing.stats.SurfacePlotterUI;
import jdplus.toolkit.desktop.plugin.ui.processing.stats.SurfacePlotterUI.Functions;
import jdplus.toolkit.base.api.timeseries.TsDocument;
import jdplus.toolkit.base.api.util.Id;
import java.util.function.Function;
import jdplus.toolkit.base.core.math.functions.IFunction;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;

/**
 *
 * @author PALATEJ
 * @param <D>
 */
public abstract class LikelihoodFactory<D extends TsDocument<?, ?>>
        extends ProcDocumentItemFactory<D, Functions> {

    private static final Function< RegSarimaModel, Functions> LLEXTRACTOR = source -> {

        if (source == null) {
            return null;
        } else {
            IFunction fn = source.likelihoodFunction();
            if (fn == null)
                return null;
            return Functions.create(fn, fn.evaluate(source.getEstimation().getParameters().getValues()));
        }
    };

    protected LikelihoodFactory(Class<D> documentType, Id id, Function<D, RegSarimaModel> extractor) {
        super(documentType, id, extractor == null ? null : extractor.andThen(LLEXTRACTOR), new SurfacePlotterUI());
    }
}
