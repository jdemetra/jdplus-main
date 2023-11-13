/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui.processing;

import jdplus.toolkit.base.api.processing.ProcDocument;
import lombok.NonNull;

/**
 *
 * @author Jean Palate
 * @param <D>
 */
public interface IProcDocumentViewFactory<D extends ProcDocument> {

    @NonNull
    IProcDocumentView<D> create(@NonNull D document);
}
