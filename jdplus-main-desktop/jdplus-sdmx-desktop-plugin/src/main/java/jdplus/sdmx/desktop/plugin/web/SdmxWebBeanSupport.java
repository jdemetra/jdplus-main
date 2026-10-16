package jdplus.sdmx.desktop.plugin.web;

import internal.sdmx.desktop.plugin.Caches;
import internal.sdmx.desktop.plugin.SdmxAutoCompletion;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jdplus.sdmx.base.api.web.SdmxWebBean;
import jdplus.sdmx.base.api.web.SdmxWebProvider;
import jdplus.toolkit.desktop.plugin.properties.NodePropertySetBuilder;
import jdplus.toolkit.desktop.plugin.tsproviders.TsProviderProperties;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import sdmxdl.Dimension;
import sdmxdl.web.WebSource;

@lombok.experimental.UtilityClass
class SdmxWebBeanSupport {

    @NbBundle.Messages({"bean.cache.description=Mechanism used to improve performance."})
    public static List<Sheet.Set> newSheet(SdmxWebBean bean, SdmxWebProvider provider) {
        ConcurrentMap<Object, Object> autoCompletionCache = Caches.ttlCacheAsMap(Duration.ofMinutes(1));

        List<Sheet.Set> result = new ArrayList<>();
        NodePropertySetBuilder b = new NodePropertySetBuilder();
        result.add(withSource(b.reset("Source"), bean, provider, autoCompletionCache)
                .build());
        result.add(withOptions(b.reset("Options"), bean, provider, autoCompletionCache)
                .build());
        result.add(withCache(b.reset("Cache").description(Bundle.bean_cache_description()), bean)
                .build());
        return result;
    }

    @NbBundle.Messages({
        "bean.source.display=Provider",
        "bean.source.description=The identifier of the service that provides data.",
        "bean.database.display=Database",
        "bean.database.description=Optional top-level grouping",
        "bean.flow.display=Dataflow",
        "bean.flow.description=The identifier of a specific dataflow.",
    })
    private static NodePropertySetBuilder withSource(
            NodePropertySetBuilder b,
            SdmxWebBean bean,
            SdmxWebProvider provider,
            ConcurrentMap<Object, Object> autoCompletionCache) {
        b.withAutoCompletion()
                .select("source", bean::getSource, bean::setSource)
                .servicePath(WebSource.class.getName())
                .display(Bundle.bean_source_display())
                .description(Bundle.bean_source_description())
                .add();

        SdmxAutoCompletion database = SdmxAutoCompletion.onDatabase(provider, bean, autoCompletionCache);

        b.withAutoCompletion()
                .select("database", bean::getDatabase, bean::setDatabase)
                .source(database.getSource())
                .cellRenderer(database.getRenderer())
                .display(Bundle.bean_database_display())
                .description(Bundle.bean_database_description())
                .add();

        SdmxAutoCompletion dataflow = SdmxAutoCompletion.onFlow(provider, bean, autoCompletionCache);

        b.withAutoCompletion()
                .select("flow", bean::getFlow, bean::setFlow)
                .source(dataflow.getSource())
                .cellRenderer(dataflow.getRenderer())
                .display(Bundle.bean_flow_display())
                .description(Bundle.bean_flow_description())
                .add();

        return b;
    }

    @NbBundle.Messages({
        "bean.dimensions.display=Dataflow dimensions",
        "bean.dimensions.description=An optional comma-separated list of dimensions that defines the order used to hierarchise time series.",
        "bean.labelAttribute.display=Series label attribute",
        "bean.labelAttribute.description=An optional attribute that carries the label of time series."
    })
    private static NodePropertySetBuilder withOptions(
            NodePropertySetBuilder b,
            SdmxWebBean bean,
            SdmxWebProvider provider,
            ConcurrentMap<Object, Object> autoCompletionCache) {
        SdmxAutoCompletion dimension = SdmxAutoCompletion.onDimension(provider, bean, autoCompletionCache);

        b.withAutoCompletion()
                .select(
                        bean,
                        "dimensions",
                        List.class,
                        list -> String.join(",", ((List<String>) list)),
                        text -> Stream.of(text.split(",", -1)).toList())
                .source(dimension.getSource())
                .cellRenderer(dimension.getRenderer())
                .separator(",")
                .defaultValueSupplier(() -> dimension.getSource().getValues("").stream()
                        .map(Dimension.class::cast)
                        .map(Dimension::getId)
                        .collect(Collectors.joining(",")))
                .display(Bundle.bean_dimensions_display())
                .description(Bundle.bean_dimensions_description())
                .add();

        SdmxAutoCompletion attribute = SdmxAutoCompletion.onAttribute(provider, bean, autoCompletionCache);

        b.withAutoCompletion()
                .select("labelAttribute", bean::getLabelAttribute, bean::setLabelAttribute)
                .source(attribute.getSource())
                .cellRenderer(attribute.getRenderer())
                .display(Bundle.bean_labelAttribute_display())
                .description(Bundle.bean_labelAttribute_description())
                .add();

        return b;
    }

    private static NodePropertySetBuilder withCache(NodePropertySetBuilder b, SdmxWebBean bean) {
        TsProviderProperties.addBulkCube(b, bean::getCache, bean::setCache);
        return b;
    }
}
