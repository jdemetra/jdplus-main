package jdplus.sdmx.desktop.plugin.file;

import internal.sdmx.desktop.plugin.SdmxIcons;
import java.io.IOException;
import java.util.Locale;
import javax.swing.*;
import jdplus.toolkit.base.tsp.util.PropertyHandler;
import jdplus.toolkit.desktop.plugin.properties.NodePropertySetBuilder;
import jdplus.toolkit.desktop.plugin.util.Persistence;
import nbbrd.design.MightBeGenerated;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.nodes.Sheet;
import sdmxdl.Languages;
import sdmxdl.file.FileSource;
import sdmxdl.file.SdmxFileManager;
import sdmxdl.file.spi.FileCaching;
import standalone_sdmxdl.nbbrd.io.text.Parser;
import standalone_sdmxdl.sdmxdl.provider.caching.MemCachingSupport;

@lombok.Data
public class SdmxFileConfiguration {

    private static final String LANGUAGES_PROPERTY = "languages";
    private static final String DEFAULT_LANGUAGES = null;
    private String languages = DEFAULT_LANGUAGES;

    private static final String NO_CACHE_PROPERTY = "noCache";
    private static final boolean DEFAULT_NO_CACHE = false;
    private boolean noCache = DEFAULT_NO_CACHE;

    @MightBeGenerated
    public static SdmxFileConfiguration copyOf(SdmxFileConfiguration bean) {
        SdmxFileConfiguration result = new SdmxFileConfiguration();
        result.languages = bean.languages;
        result.noCache = bean.noCache;
        return result;
    }

    public SdmxFileManager toSdmxFileManager() {
        return SdmxFileManager.ofServiceLoader().toBuilder()
                .onEvent(source -> (marker, message) -> reportEvent(source, marker, message))
                .onError(source -> (marker, message, error) -> reportError(source, marker, message, error))
                .caching(toCaching())
                .build();
    }

    public Languages toLanguages() {
        return Parser.of(Languages::parse).parseValue(languages).orElse(Languages.ANY);
    }

    private void reportEvent(FileSource source, String marker, CharSequence message) {
        StatusDisplayer.getDefault().setStatusText(message.toString());
    }

    private void reportError(FileSource source, String marker, CharSequence message, IOException error) {
        NotificationDisplayer.getDefault().notify(message.toString(), SdmxIcons.getDefaultIcon(16), "", null);
    }

    private FileCaching toCaching() {
        return noCache
                ? FileCaching.noOp()
                : MemCachingSupport.builder()
                        .id("SHARED_SOFT_MEM")
                        //                .repositoriesOf(Caches.softValuesCacheAsMap())
                        //                .webMonitorsOf(Caches.softValuesCacheAsMap())
                        .build();
    }

    Sheet toSheet() {
        Sheet result = new Sheet();
        NodePropertySetBuilder b = new NodePropertySetBuilder();

        b.withAutoCompletion()
                .select(this, LANGUAGES_PROPERTY)
                .servicePath(Locale.class.getName())
                .separator(",")
                .display("Languages")
                .description("Language priority list")
                .add();
        result.put(b.build());

        b.reset("Other");
        b.withBoolean()
                .select(this, NO_CACHE_PROPERTY)
                .display("No cache")
                .description("Disable caching")
                .add();
        result.put(b.build());

        return result;
    }

    @MightBeGenerated
    static final Persistence<SdmxFileConfiguration> PERSISTENCE = Persistence.builderOf(SdmxFileConfiguration.class)
            .name("INSTANCE")
            .version("20230717")
            .with(
                    PropertyHandler.onString(LANGUAGES_PROPERTY, DEFAULT_LANGUAGES),
                    SdmxFileConfiguration::getLanguages,
                    SdmxFileConfiguration::setLanguages)
            .with(
                    PropertyHandler.onBoolean(NO_CACHE_PROPERTY, DEFAULT_NO_CACHE),
                    SdmxFileConfiguration::isNoCache,
                    SdmxFileConfiguration::setNoCache)
            .build();
}
