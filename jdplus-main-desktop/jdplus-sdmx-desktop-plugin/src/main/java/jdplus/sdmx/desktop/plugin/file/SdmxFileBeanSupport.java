package jdplus.sdmx.desktop.plugin.file;

import internal.sdmx.desktop.plugin.Caches;
import internal.sdmx.desktop.plugin.SdmxAutoCompletion;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jdplus.sdmx.base.api.file.SdmxFileBean;
import jdplus.sdmx.base.api.file.SdmxFileProvider;
import jdplus.toolkit.desktop.plugin.properties.NodePropertySetBuilder;
import jdplus.toolkit.desktop.plugin.ui.properties.FileLoaderFileFilter;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import sdmxdl.Dimension;

@lombok.experimental.UtilityClass
class SdmxFileBeanSupport {

    @NbBundle.Messages({"bean.cache.description=Mechanism used to improve performance."})
    public static List<Sheet.Set> newSheet(SdmxFileBean bean, SdmxFileProvider provider) {
        ConcurrentMap<Object, Object> autoCompletionCache = Caches.ttlCacheAsMap(Duration.ofMinutes(1));

        List<Sheet.Set> result = new ArrayList<>();
        NodePropertySetBuilder b = new NodePropertySetBuilder();
        result.add(withSource(b.reset("Source"), bean, provider).build());
        result.add(withOptions(b.reset("Options"), bean, provider, autoCompletionCache)
                .build());
        return result;
    }

    @NbBundle.Messages({
        "bean.file.display=Data file",
        "bean.file.description=The path to the sdmx data file.",
    })
    private static NodePropertySetBuilder withSource(
            NodePropertySetBuilder b, SdmxFileBean bean, SdmxFileProvider provider) {
        b.withFile()
                .select("file", bean::getFile, bean::setFile)
                .display(Bundle.bean_file_display())
                .description(Bundle.bean_file_description())
                .filterForSwing(new FileLoaderFileFilter(provider))
                .paths(provider.getPaths())
                .directories(false)
                .add();
        return b;
    }

    @NbBundle.Messages({
        "bean.structureFile.display=Structure file",
        "bean.structureFile.description=The path to the sdmx structure file.",
        "bean.dialect.display=Dialect",
        "bean.dialect.description=The name of the dialect used to parse the sdmx data file.",
        "bean.dimensions.display=Dataflow dimensions",
        "bean.dimensions.description=An optional comma-separated list of dimensions that defines the order used to hierarchise time series.",
        "bean.labelAttribute.display=Series label attribute",
        "bean.labelAttribute.description=An optional attribute that carries the label of time series."
    })
    private static NodePropertySetBuilder withOptions(
            NodePropertySetBuilder b,
            SdmxFileBean bean,
            SdmxFileProvider provider,
            ConcurrentMap<Object, Object> autoCompletionCache) {
        b.withFile()
                .select("structureFile", bean::getStructureFile, bean::setStructureFile)
                .display(Bundle.bean_structureFile_display())
                .description(Bundle.bean_structureFile_description())
                .filterForSwing(new FileLoaderFileFilter(provider))
                .paths(provider.getPaths())
                .directories(false)
                .add();

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
}
