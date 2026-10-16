package jdplus.sdmx.desktop.plugin.web;

import internal.sdmx.desktop.plugin.SdmxIcons;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.logging.Level;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import jdplus.sdmx.desktop.plugin.Toggle;
import jdplus.toolkit.base.tsp.util.PropertyHandler;
import jdplus.toolkit.desktop.plugin.properties.NodePropertySetBuilder;
import jdplus.toolkit.desktop.plugin.util.Persistence;
import nbbrd.design.MightBeGenerated;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.nodes.Sheet;
import sdmxdl.Languages;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;
import standalone_sdmxdl.nbbrd.io.text.BooleanProperty;
import standalone_sdmxdl.nbbrd.io.text.Parser;
import standalone_sdmxdl.nbbrd.io.text.Property;
import standalone_sdmxdl.sdmxdl.provider.PropertiesSupport;
import standalone_sdmxdl.sdmxdl.provider.ri.caching.RiCaching;
import standalone_sdmxdl.sdmxdl.provider.ri.networking.RiNetworking;
import standalone_sdmxdl.sdmxdl.provider.ri.registry.RiRegistry;

@lombok.extern.java.Log
@lombok.Data
public class SdmxWebConfiguration {

    private static final String SOURCES_PROPERTY = "sources";
    private static final File DEFAULT_SOURCES = null;
    private File sources = DEFAULT_SOURCES;

    private static final String LANGUAGES_PROPERTY = "languages";
    private static final String DEFAULT_LANGUAGES = null;
    private String languages = DEFAULT_LANGUAGES;

    private static final String CURL_BACKEND_PROPERTY = "curlBackend";
    private static final Toggle DEFAULT_CURL_BACKEND = Toggle.DEFAULT;
    private Toggle curlBackend = DEFAULT_CURL_BACKEND;

    private static final String NO_CACHE_PROPERTY = "noCache";
    private static final Toggle DEFAULT_NO_CACHE = Toggle.DEFAULT;
    private Toggle noCache = DEFAULT_NO_CACHE;

    private static final String AUTO_PROXY_PROPERTY = "autoProxy";
    private static final Toggle DEFAULT_AUTO_PROXY = Toggle.DEFAULT;
    private Toggle autoProxy = DEFAULT_AUTO_PROXY;

    private static final String NO_DEFAULT_SSL_PROPERTY = "noDefaultSSL";
    private static final Toggle DEFAULT_NO_DEFAULT_SSL = Toggle.DEFAULT;
    private Toggle noDefaultSSL = DEFAULT_NO_DEFAULT_SSL;

    private static final String NO_SYSTEM_SSL_PROPERTY = "noSystemSSL";
    private static final Toggle DEFAULT_NO_SYSTEM_SSL = Toggle.DEFAULT;
    private Toggle noSystemSSL = DEFAULT_NO_SYSTEM_SSL;

    private static final String DISPLAY_CODES_PROPERTY = "displayCodes";
    private static final boolean DEFAULT_DISPLAY_CODES = false;
    private boolean displayCodes = DEFAULT_DISPLAY_CODES;

    private static final String LOG_EVENTS_PROPERTY = "logEvents";
    private static final boolean DEFAULT_LOG_EVENTS = false;
    private boolean logEvents = DEFAULT_LOG_EVENTS;

    @MightBeGenerated
    public static SdmxWebConfiguration copyOf(SdmxWebConfiguration bean) {
        SdmxWebConfiguration result = new SdmxWebConfiguration();
        result.sources = bean.sources;
        result.languages = bean.languages;
        result.curlBackend = bean.curlBackend;
        result.noCache = bean.noCache;
        result.autoProxy = bean.autoProxy;
        result.noDefaultSSL = bean.noDefaultSSL;
        result.noSystemSSL = bean.noSystemSSL;
        result.displayCodes = bean.displayCodes;
        result.logEvents = bean.logEvents;
        return result;
    }

    public SdmxWebManager toSdmxWebManager() {
        Properties properties = System.getProperties();

        if (sources != null && !sources.getPath().isEmpty()) {
            properties.setProperty(RiRegistry.SOURCES_FILE_PROPERTY.getKey(), sources.getPath());
        } else {
            properties.remove(RiRegistry.SOURCES_FILE_PROPERTY.getKey());
        }

        curlBackend.applyTo(properties, RiNetworking.URL_BACKEND_PROPERTY, "JDK", "CURL");
        noCache.applyTo(properties, RiCaching.NO_CACHE_PROPERTY);
        autoProxy.applyTo(properties, RiNetworking.AUTO_PROXY_PROPERTY);
        noDefaultSSL.applyTo(properties, RiNetworking.NO_DEFAULT_SSL_PROPERTY);
        noSystemSSL.applyTo(properties, RiNetworking.NO_SYSTEM_SSL_PROPERTY);

        logConfig();

        return SdmxWebManager.ofServiceLoader().toBuilder()
                .onEvent(source -> (marker, message) -> reportEvent(source, marker, message))
                .onError(source -> (marker, message, error) -> reportError(source, marker, message, error))
                .build();
    }

    private void logConfig() {
        Function<? super String, ? extends CharSequence> properties =
                key -> PropertiesSupport.getProperty(Map.of(), key);
        if (log.isLoggable(Level.INFO)) {
            for (Property<?> p :
                    new Property<?>[] {RiRegistry.SOURCES_FILE_PROPERTY, RiNetworking.URL_BACKEND_PROPERTY}) {
                log.log(Level.INFO, p.getKey() + ": " + p.get(properties));
            }
            for (BooleanProperty p : new BooleanProperty[] {
                RiCaching.NO_CACHE_PROPERTY,
                RiNetworking.AUTO_PROXY_PROPERTY,
                RiNetworking.NO_DEFAULT_SSL_PROPERTY,
                RiNetworking.NO_SYSTEM_SSL_PROPERTY
            }) {
                log.log(Level.INFO, p.getKey() + ": " + p.get(properties));
            }
        }
    }

    public Languages toLanguages() {
        return Parser.of(Languages::parse).parseValue(languages).orElse(Languages.ANY);
    }

    private void reportEvent(WebSource source, String marker, CharSequence message) {
        StatusDisplayer.getDefault().setStatusText(message.toString());
        if (logEvents) {
            log.log(Level.INFO, () -> asLogMessage(source, marker, message));
        }
    }

    private void reportError(WebSource source, String marker, CharSequence message, IOException error) {
        NotificationDisplayer.getDefault().notify(message.toString(), SdmxIcons.getDefaultIcon(16), "", null);
        log.log(Level.SEVERE, error, () -> asLogMessage(source, marker, message));
    }

    private static String asLogMessage(WebSource source, String marker, CharSequence message) {
        return "[" + source.getId() + "] (" + marker + ") " + message;
    }

    Sheet toSheet() {
        Sheet result = new Sheet();
        NodePropertySetBuilder b = new NodePropertySetBuilder();

        b.withFile()
                .select(this, SOURCES_PROPERTY)
                .display("Sources")
                .description("File that provides data source definitions")
                .filterForSwing(new FileNameExtensionFilter("XML file", "xml"))
                .directories(false)
                .add();
        b.withAutoCompletion()
                .select(this, LANGUAGES_PROPERTY)
                .servicePath(Locale.class.getName())
                .separator(",")
                .display("Languages")
                .description("Language priority list")
                .add();
        result.put(b.build());

        b.reset("Network");
        b.withEnum(Toggle.class)
                .select(this, CURL_BACKEND_PROPERTY)
                .display("Curl backend")
                .description("Use curl backend instead of JDK")
                .add();
        b.withEnum(Toggle.class)
                .select(this, NO_CACHE_PROPERTY)
                .display("No cache")
                .description("Disable caching")
                .add();
        b.withEnum(Toggle.class)
                .select(this, AUTO_PROXY_PROPERTY)
                .display("Auto proxy")
                .description("Enable automatic proxy detection")
                .add();
        b.withEnum(Toggle.class)
                .select(this, NO_DEFAULT_SSL_PROPERTY)
                .display("No default SSL")
                .description("Disable default truststore")
                .add();
        b.withEnum(Toggle.class)
                .select(this, NO_SYSTEM_SSL_PROPERTY)
                .display("No system SSL")
                .description("Disable system truststore")
                .add();
        b.withBoolean()
                .select(this, LOG_EVENTS_PROPERTY)
                .display("Log events")
                .description("Log events in IDE logs")
                .add();
        result.put(b.build());

        return result;
    }

    @MightBeGenerated
    static final Persistence<SdmxWebConfiguration> PERSISTENCE = Persistence.builderOf(SdmxWebConfiguration.class)
            .name("INSTANCE")
            .version("20230717")
            .with(
                    PropertyHandler.onFile(SOURCES_PROPERTY, DEFAULT_SOURCES),
                    SdmxWebConfiguration::getSources,
                    SdmxWebConfiguration::setSources)
            .with(
                    PropertyHandler.onString(LANGUAGES_PROPERTY, DEFAULT_LANGUAGES),
                    SdmxWebConfiguration::getLanguages,
                    SdmxWebConfiguration::setLanguages)
            .with(
                    PropertyHandler.onEnum(CURL_BACKEND_PROPERTY, DEFAULT_CURL_BACKEND),
                    SdmxWebConfiguration::getCurlBackend,
                    SdmxWebConfiguration::setCurlBackend)
            .with(
                    PropertyHandler.onEnum(NO_CACHE_PROPERTY, DEFAULT_NO_CACHE),
                    SdmxWebConfiguration::getNoCache,
                    SdmxWebConfiguration::setNoCache)
            .with(
                    PropertyHandler.onEnum(AUTO_PROXY_PROPERTY, DEFAULT_AUTO_PROXY),
                    SdmxWebConfiguration::getAutoProxy,
                    SdmxWebConfiguration::setAutoProxy)
            .with(
                    PropertyHandler.onEnum(NO_DEFAULT_SSL_PROPERTY, DEFAULT_NO_DEFAULT_SSL),
                    SdmxWebConfiguration::getNoDefaultSSL,
                    SdmxWebConfiguration::setNoDefaultSSL)
            .with(
                    PropertyHandler.onEnum(NO_SYSTEM_SSL_PROPERTY, DEFAULT_NO_SYSTEM_SSL),
                    SdmxWebConfiguration::getNoSystemSSL,
                    SdmxWebConfiguration::setNoSystemSSL)
            .with(
                    PropertyHandler.onBoolean(DISPLAY_CODES_PROPERTY, DEFAULT_DISPLAY_CODES),
                    SdmxWebConfiguration::isDisplayCodes,
                    SdmxWebConfiguration::setDisplayCodes)
            .with(
                    PropertyHandler.onBoolean(LOG_EVENTS_PROPERTY, DEFAULT_LOG_EVENTS),
                    SdmxWebConfiguration::isLogEvents,
                    SdmxWebConfiguration::setLogEvents)
            .build();
}
