/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.ui.processing;

import jdplus.toolkit.base.api.design.ExtensionPoint;
import jdplus.toolkit.desktop.plugin.util.NetBeansServiceBackend;
import jdplus.toolkit.base.api.processing.ProcDocument;
import jdplus.toolkit.base.api.util.Id;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import nbbrd.service.ServiceSorter;

/**
 *
 * @author Philippe Charles
 */
@ExtensionPoint
@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE,
        backend = NetBeansServiceBackend.class,
        singleton = true
)
public interface IProcDocumentItemFactory {

    @ServiceSorter
    int getPosition();

    @NonNull
    Class<? extends ProcDocument> getDocumentType();

    @NonNull
    Id getItemId();

    @NonNull
    JComponent getView(@NonNull ProcDocument doc) throws IllegalArgumentException;

    @Nullable
    default Icon getIcon() {
        return null;
    }

    @Nullable
    default Action[] getActions() {
        return null;
    }
}
