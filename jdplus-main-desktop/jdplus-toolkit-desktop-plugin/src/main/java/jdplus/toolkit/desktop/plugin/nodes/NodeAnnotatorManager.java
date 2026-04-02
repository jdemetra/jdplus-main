package jdplus.toolkit.desktop.plugin.nodes;

import jdplus.main.desktop.design.GlobalService;
import jdplus.toolkit.desktop.plugin.util.CollectionSupplier;
import jdplus.toolkit.desktop.plugin.util.FixmeCollectionSupplier;
import jdplus.toolkit.desktop.plugin.util.LazyGlobalService;
import lombok.NonNull;
import nbbrd.design.MightBeGenerated;
import org.openide.nodes.Node;

import java.awt.*;

import static jdplus.toolkit.desktop.plugin.util.NetBeansServiceBackend.*;

@GlobalService
public final class NodeAnnotatorManager {

    @NonNull
    public static NodeAnnotatorManager get() {
        return LazyGlobalService.get(NodeAnnotatorManager.class, NodeAnnotatorManager::new);
    }

    private NodeAnnotatorManager() {
    }

    private final CollectionSupplier<NodeAnnotatorSpi> providers = FixmeCollectionSupplier.of(NodeAnnotatorSpi.class, buildServiceLoader()::get);

    @MightBeGenerated
    private static NodeAnnotatorSpiLoader buildServiceLoader() {
        return NodeAnnotatorSpiLoader.builder().backend(lookupFactory(), lookupStreamer(), lookupReloader()).build();
    }

    public Image annotateIcon(Node node, Image image) {
        Image result = image;
        for (NodeAnnotatorSpi o : providers.get()) {
            result = o.annotateIcon(node, result);
        }
        return result;
    }

    public String annotateName(Node node, String name) {
        String result = name;
        for (NodeAnnotatorSpi o : providers.get()) {
            result = o.annotateName(node, result);
        }
        return result;
    }
}
