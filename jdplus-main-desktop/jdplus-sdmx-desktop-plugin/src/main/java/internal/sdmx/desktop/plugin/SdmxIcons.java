package internal.sdmx.desktop.plugin;

import java.io.IOException;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.*;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import nbbrd.design.ReturnNew;
import nbbrd.design.swing.OnEDT;
import nbbrd.desktop.favicon.DomainName;
import nbbrd.desktop.favicon.FaviconRef;
import nbbrd.desktop.favicon.FaviconSupport;
import nbbrd.desktop.favicon.URLConnectionFactory;
import org.openide.util.ImageUtilities;
import sdmxdl.Confidentiality;
import sdmxdl.swing.SdmxLogo;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.Network;
import sdmxdl.web.spi.Networking;
import sdmxdl.web.spi.SSLFactory;

@lombok.experimental.UtilityClass
public class SdmxIcons {

    @OnEDT
    public static @NonNull ImageIcon getDefaultIcon(int size) {
        return LOGOS.computeIfAbsent(size, SdmxIcons::newSdmxLogo);
    }

    @OnEDT
    public static @NonNull Icon getFavicon(@NonNull Networking networking, @NonNull WebSource source, int size) {
        ImageIcon defaultIcon = getDefaultIcon(size);
        URL website = source.getWebsite();
        return website != null && isAllowed(source.getConfidentiality())
                ? getFavicons(networking)
                        .getOrDefault(FaviconRef.of(DomainName.of(source.getWebsite()), size), defaultIcon)
                : defaultIcon;
    }

    @OnEDT
    public static @NonNull Icon getFavicon(
            @NonNull Networking networking, @NonNull WebSource source, @NonNull Runnable callback, int size) {
        ImageIcon defaultIcon = getDefaultIcon(size);
        URL website = source.getWebsite();
        return website != null && isAllowed(source.getConfidentiality())
                ? getFavicons(networking)
                        .getOrDefault(FaviconRef.of(DomainName.of(website), size), callback, defaultIcon)
                : defaultIcon;
    }

    private static boolean isAllowed(Confidentiality confidentiality) {
        return confidentiality.compareTo(MAX_CONFIDENTIALITY) <= 0;
    }

    private static final Confidentiality MAX_CONFIDENTIALITY = Confidentiality.PUBLIC;

    private static FaviconSupport getFavicons(Networking networking) {
        return FAVICONS.toBuilder()
                .client(new FaviconClientOverCustomNetworking(networking))
                .build();
    }

    @ReturnNew
    private static @NonNull ImageIcon newSdmxLogo(int size) {
        return new ImageIcon(ImageUtilities.icon2Image(new SdmxLogo(size)));
    }

    private static final Map<Integer, ImageIcon> LOGOS = new HashMap<>();

    private static final FaviconSupport FAVICONS = FaviconSupport.ofServiceLoader().toBuilder()
            .cache(Caches.ttlCacheAsMap(Duration.ofHours(1)))
            .build();

    @AllArgsConstructor
    private static final class FaviconClientOverCustomNetworking implements URLConnectionFactory {

        private final @NonNull Networking networking;

        @Override
        public @NonNull URLConnection openConnection(@NonNull URL url) throws IOException {
            Network network = networking.getNetwork(asSource(url), null, null);
            Proxy proxy = selectProxy(network, url);
            URLConnection result = network.getURLConnectionFactory().openConnection(url, proxy);
            applyHttps(result, network);
            return result;
        }

        private static WebSource asSource(URL url) throws IOException {
            try {
                return WebSource.builder()
                        .id("")
                        .endpoint(url.toURI())
                        .driver("")
                        .build();
            } catch (URISyntaxException ex) {
                throw new IOException(ex);
            }
        }

        private static void applyHttps(URLConnection result, Network network) {
            if (result instanceof HttpsURLConnection) {
                HttpsURLConnection https = (HttpsURLConnection) result;
                SSLFactory sslFactory = network.getSSLFactory();
                https.setHostnameVerifier(sslFactory.getHostnameVerifier());
                https.setSSLSocketFactory(sslFactory.getSSLSocketFactory());
            }
        }

        private static Proxy selectProxy(Network network, URL url) throws IOException {
            try {
                return network.getProxySelector().select(url.toURI()).stream()
                        .findFirst()
                        .orElse(Proxy.NO_PROXY);
            } catch (URISyntaxException ex) {
                throw new IOException(ex);
            }
        }
    }
}
