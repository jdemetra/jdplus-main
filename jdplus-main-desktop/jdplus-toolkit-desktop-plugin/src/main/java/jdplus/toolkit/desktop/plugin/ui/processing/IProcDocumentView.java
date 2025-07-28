/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui.processing;

import jdplus.toolkit.base.api.processing.ProcDocument;
import jdplus.toolkit.desktop.plugin.interfaces.Disposable;
import jdplus.toolkit.base.api.util.Id;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import javax.swing.*;
import java.util.List;

/**
 * @author Jean Palate
 * @param <D>
 */
public interface IProcDocumentView<D extends ProcDocument> extends Disposable {

    @NonNull
    D getDocument();

    @NonNull
    List<Id> getItems();

    @Nullable
    JComponent getView(Id path);

    @Nullable
    Icon getIcon(Id path);

    @Nullable
    Action[] getActions(Id path);

    @Nullable
    Id getPreferredView();

    void refresh();
}
