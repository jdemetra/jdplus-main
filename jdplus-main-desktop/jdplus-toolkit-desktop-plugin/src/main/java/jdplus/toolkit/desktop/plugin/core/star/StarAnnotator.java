/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.desktop.plugin.core.star;

import jdplus.toolkit.desktop.plugin.DemetraIcons;
import jdplus.toolkit.desktop.plugin.nodes.NodeAnnotatorSpi;
import jdplus.toolkit.desktop.plugin.star.StarListManager;
import jdplus.toolkit.base.tsp.DataSource;
import lombok.NonNull;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

import java.awt.*;

/**
 * @author Philippe Charles
 */
@ServiceProvider(service = NodeAnnotatorSpi.class)
public final class StarAnnotator implements NodeAnnotatorSpi {

    boolean isStarred(Node node) {
        DataSource dataSource = node.getLookup().lookup(DataSource.class);
        return dataSource != null && StarListManager.get().isStarred(dataSource);
    }

    @Override
    public @NonNull Image annotateIcon(@NonNull Node node, @NonNull Image image) {
        if (isStarred(node)) {
            Image badge = DemetraIcons.BULLET_STAR.getImageIcon().getImage();
            return ImageUtilities.mergeImages(image, badge, 10, 0);
        }
        return image;
    }

    @Override
    public @NonNull String annotateName(@NonNull Node node, @NonNull String name) {
        return name;
    }
}
