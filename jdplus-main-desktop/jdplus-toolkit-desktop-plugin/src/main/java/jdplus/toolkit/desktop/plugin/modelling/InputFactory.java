/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.toolkit.desktop.plugin.modelling;

import jdplus.toolkit.desktop.plugin.ui.processing.ProcDocumentItemFactory;
import jdplus.toolkit.desktop.plugin.ui.processing.TsViewToolkit;
import jdplus.toolkit.base.api.timeseries.Ts;
import jdplus.toolkit.base.api.timeseries.TsDocument;
import jdplus.toolkit.base.api.util.Id;
import java.util.Collections;

/**
 *
 * @author PALATEJ
 * @param <D>
 */
public abstract class InputFactory<D extends TsDocument<?, ?>> extends ProcDocumentItemFactory<D, Ts> {

    protected InputFactory(Class<D> documentType, Id id) {
        super(documentType, id,
                source -> source.getInput(),
                s -> TsViewToolkit.getGrid(s == null ? null : Collections.singleton(s)));
    }
}
