/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.nodes;

import jdplus.toolkit.base.api.design.ExtensionPoint;
import jdplus.toolkit.desktop.plugin.util.NetBeansServiceBackend;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import lombok.NonNull;
import org.openide.nodes.Node;

import java.awt.*;

/**
 *
 * @author Philippe Charles
 */
@ExtensionPoint
@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE,
        backend = NetBeansServiceBackend.class
)
public interface NodeAnnotatorSpi {

    @NonNull
    Image annotateIcon(@NonNull Node node, @NonNull Image image);

    @NonNull
    String annotateName(@NonNull Node node, @NonNull String name);
}
