/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.toolkit.desktop.plugin.modelling;

import jdplus.toolkit.desktop.plugin.ui.processing.ProcDocumentItemFactory;
import jdplus.toolkit.desktop.plugin.ui.processing.stats.RegressorsUI;
import jdplus.toolkit.base.api.timeseries.TsDocument;
import jdplus.toolkit.base.api.util.Id;
import java.util.function.Function;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;

/**
 *
 * @author PALATEJ
 * @param <D>
 */
public abstract class ModelRegressorsFactory<D extends TsDocument<?, ?>>
            extends ProcDocumentItemFactory<D, RegSarimaModel> {

        protected ModelRegressorsFactory(Class<D> documentType, Id id, Function<D, RegSarimaModel> extractor) {
            super(documentType, id, extractor, new RegressorsUI());
        }
    }
