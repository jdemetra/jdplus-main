/*
 * Copyright 2017 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package internal.sdmx.desktop.plugin;

import static ec.util.completion.AutoCompletionSource.Behavior.*;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import ec.util.completion.AutoCompletionSource;
import ec.util.completion.ExtAutoCompletionSource;
import ec.util.completion.swing.CustomListCellRenderer;
import internal.sdmx.base.api.SdmxBeans;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.swing.*;
import jdplus.sdmx.base.api.HasSdmxProperties;
import jdplus.sdmx.base.api.file.SdmxFileBean;
import jdplus.sdmx.base.api.file.SdmxFileProvider;
import jdplus.sdmx.base.api.web.SdmxWebBean;
import jdplus.sdmx.base.api.web.SdmxWebProvider;
import lombok.NonNull;
import sdmxdl.*;
import sdmxdl.file.FileSource;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;

/**
 * @author Philippe Charles
 */
public abstract class SdmxAutoCompletion {

    public abstract @NonNull AutoCompletionSource getSource();

    public abstract @NonNull ListCellRenderer<?> getRenderer();

    public static @NonNull SdmxAutoCompletion onWebSource(@NonNull SdmxWebProvider provider) {
        return new WebSourceCompletion(provider);
    }

    public static @NonNull SdmxAutoCompletion onDatabase(
            @NonNull SdmxWebProvider provider,
            @NonNull SdmxWebBean bean,
            @NonNull ConcurrentMap<Object, Object> cache) {
        return new DatabaseCompletion<>(provider, () -> SdmxBeans.getWebSourceOrNull(bean, provider), cache);
    }

    public static @NonNull SdmxAutoCompletion onFlow(
            @NonNull SdmxWebProvider provider,
            @NonNull SdmxWebBean bean,
            @NonNull ConcurrentMap<Object, Object> cache) {
        return new FlowCompletion<>(
                provider, () -> SdmxBeans.getWebSourceOrNull(bean, provider), () -> SdmxBeans.getDatabase(bean), cache);
    }

    public static @NonNull SdmxAutoCompletion onDimension(
            @NonNull SdmxWebProvider provider,
            @NonNull SdmxWebBean bean,
            @NonNull ConcurrentMap<Object, Object> cache) {
        return new DimensionCompletion<>(
                provider,
                () -> SdmxBeans.getWebSourceOrNull(bean, provider),
                () -> SdmxBeans.getDatabase(bean),
                () -> SdmxBeans.getFlowRefOrNull(bean),
                cache);
    }

    public static @NonNull SdmxAutoCompletion onDimension(
            @NonNull SdmxFileProvider provider,
            @NonNull SdmxFileBean bean,
            @NonNull ConcurrentMap<Object, Object> cache) {
        return new DimensionCompletion<>(
                provider,
                () -> SdmxBeans.getFileSource(bean, provider).orElse(null),
                () -> SdmxBeans.getDatabase(bean),
                () -> SdmxBeans.getFileSource(bean, provider)
                        .map(FileSource::asDataflowRef)
                        .orElse(null),
                cache);
    }

    public static @NonNull SdmxAutoCompletion onAttribute(
            @NonNull SdmxWebProvider provider,
            @NonNull SdmxWebBean bean,
            @NonNull ConcurrentMap<Object, Object> cache) {
        return new AttributeCompletion<>(
                provider,
                () -> SdmxBeans.getWebSourceOrNull(bean, provider),
                () -> SdmxBeans.getDatabase(bean),
                () -> SdmxBeans.getFlowRefOrNull(bean),
                cache);
    }

    public static @NonNull SdmxAutoCompletion onAttribute(
            @NonNull SdmxFileProvider provider,
            @NonNull SdmxFileBean bean,
            @NonNull ConcurrentMap<Object, Object> cache) {
        return new AttributeCompletion<>(
                provider,
                () -> SdmxBeans.getFileSource(bean, provider).orElse(null),
                () -> SdmxBeans.getDatabase(bean),
                () -> SdmxBeans.getFileSource(bean, provider)
                        .map(FileSource::asDataflowRef)
                        .orElse(null),
                cache);
    }

    @lombok.AllArgsConstructor
    private static final class WebSourceCompletion extends SdmxAutoCompletion {

        private final @NonNull HasSdmxProperties<SdmxWebManager> provider;

        @Override
        public @NonNull AutoCompletionSource getSource() {
            return ExtAutoCompletionSource.builder(this::load)
                    .behavior(SYNC)
                    .postProcessor(this::filterAndSort)
                    .valueToString(WebSource::getId)
                    .build();
        }

        @Override
        public @NonNull ListCellRenderer<?> getRenderer() {
            return new CustomListCellRenderer<WebSource>() {
                @Override
                protected @NonNull String getValueAsString(@NonNull WebSource value) {
                    return value.getId() + ": " + provider.getLanguages().select(value.getNames());
                }

                @Override
                protected Icon toIcon(
                        String term, JList list, WebSource value, int index, boolean isSelected, boolean cellHasFocus) {
                    return SdmxIcons.getFavicon(provider.getSdmxManager().getNetworking(), value, list::repaint, 16);
                }
            };
        }

        private List<WebSource> load(String term) {
            return provider.getSdmxManager().getSources().values().stream()
                    .filter(source -> !source.isAlias())
                    .collect(toList());
        }

        private List<WebSource> filterAndSort(List<WebSource> list, String term) {
            return list.stream().filter(getFilter(term)).collect(toList());
        }

        private Predicate<WebSource> getFilter(String term) {
            Predicate<String> filter = ExtAutoCompletionSource.basicFilter(term);
            return value -> filter.test(provider.getLanguages().select(value.getNames()))
                    || filter.test(value.getId())
                    || value.getAliases().stream().anyMatch(filter);
        }
    }

    @lombok.AllArgsConstructor
    private static final class DatabaseCompletion<S extends Source> extends SdmxAutoCompletion {

        private final @NonNull HasSdmxProperties<? extends SdmxManager<S>> provider;

        private final @NonNull Supplier<S> source;

        private final @NonNull ConcurrentMap<Object, Object> cache;

        @Override
        public @NonNull AutoCompletionSource getSource() {
            return ExtAutoCompletionSource.builder(this::load)
                    .behavior(this::getBehavior)
                    .postProcessor(this::filterAndSort)
                    .valueToString(o -> o.getRef().toString())
                    .cache(cache, this::getCacheKey, SYNC)
                    .build();
        }

        @Override
        public @NonNull ListCellRenderer<?> getRenderer() {
            return CustomListCellRenderer.<Database>of(
                    flow -> flow.getRef() + "<br><i>" + flow.getName(),
                    flow -> flow.getRef().toString());
        }

        private List<Database> load(String term) throws Exception {
            return new ArrayList<>(provider.getSdmxManager()
                    .using(source.get())
                    .getDatabases(SourceRequest.builder()
                            .languages(provider.getLanguages())
                            .build()));
        }

        private AutoCompletionSource.Behavior getBehavior(String term) {
            return source.get() != null ? ASYNC : NONE;
        }

        private List<Database> filterAndSort(List<Database> values, String term) {
            Predicate<String> filter = ExtAutoCompletionSource.basicFilter(term);
            return values.stream()
                    .filter(o ->
                            filter.test(o.getName()) || filter.test(o.getRef().getId()))
                    .sorted(comparing(Database::getName))
                    .collect(toList());
        }

        private String getCacheKey(String term) {
            return "Database" + source.get() + provider.getLanguages();
        }
    }

    @lombok.AllArgsConstructor
    private static final class FlowCompletion<S extends Source> extends SdmxAutoCompletion {

        private final @NonNull HasSdmxProperties<? extends SdmxManager<S>> provider;

        private final @NonNull Supplier<S> source;

        private final @NonNull Supplier<DatabaseRef> database;

        private final @NonNull ConcurrentMap<Object, Object> cache;

        @Override
        public @NonNull AutoCompletionSource getSource() {
            return ExtAutoCompletionSource.builder(this::load)
                    .behavior(this::getBehavior)
                    .postProcessor(this::filterAndSort)
                    .valueToString(o -> o.getRef().toShortString())
                    .cache(cache, this::getCacheKey, SYNC)
                    .build();
        }

        @Override
        public @NonNull ListCellRenderer<?> getRenderer() {
            return CustomListCellRenderer.<Flow>of(
                    flow -> flow.getRef().toShortString() + "<br><i>" + flow.getName(),
                    flow -> flow.getRef().toString());
        }

        private List<Flow> load(String term) throws Exception {
            return new ArrayList<>(provider.getSdmxManager()
                    .using(source.get())
                    .getFlows(DatabaseRequest.builder()
                            .languages(provider.getLanguages())
                            .database(database.get())
                            .build()));
        }

        private AutoCompletionSource.Behavior getBehavior(String term) {
            return source.get() != null ? ASYNC : NONE;
        }

        private List<Flow> filterAndSort(List<Flow> values, String term) {
            Predicate<String> filter = ExtAutoCompletionSource.basicFilter(term);
            return values.stream()
                    .filter(o -> filter.test(o.getName())
                            || filter.test(o.getRef().getId())
                            || filter.test(o.getDescription()))
                    .sorted(comparing(Flow::getName))
                    .collect(toList());
        }

        private String getCacheKey(String term) {
            return "Dataflow" + source.get() + database.get() + provider.getLanguages();
        }
    }

    @lombok.AllArgsConstructor
    private static final class DimensionCompletion<S extends Source> extends SdmxAutoCompletion {

        private final @NonNull HasSdmxProperties<? extends SdmxManager<S>> provider;

        private final @NonNull Supplier<S> source;

        private final @NonNull Supplier<DatabaseRef> database;

        private final @NonNull Supplier<FlowRef> flowRef;

        private final @NonNull ConcurrentMap<Object, Object> cache;

        @Override
        public @NonNull AutoCompletionSource getSource() {
            return ExtAutoCompletionSource.builder(this::load)
                    .behavior(this::getBehavior)
                    .postProcessor(this::filterAndSort)
                    .valueToString(Dimension::getId)
                    .cache(cache, this::getCacheKey, SYNC)
                    .build();
        }

        @Override
        public @NonNull ListCellRenderer<?> getRenderer() {
            return CustomListCellRenderer.of(Dimension::getId, Dimension::getName);
        }

        private List<Dimension> load(String term) throws Exception {
            return provider.getSdmxManager()
                    .using(source.get())
                    .getMeta(FlowRequest.builder()
                            .languages(provider.getLanguages())
                            .database(database.get())
                            .flow(flowRef.get())
                            .build())
                    .getStructure()
                    .getDimensions();
        }

        private AutoCompletionSource.Behavior getBehavior(String term) {
            return source.get() != null && flowRef.get() != null ? ASYNC : NONE;
        }

        private List<Dimension> filterAndSort(List<Dimension> values, String term) {
            Predicate<String> filter = ExtAutoCompletionSource.basicFilter(term);
            return values.stream()
                    .filter(o -> filter.test(o.getId()) || filter.test(o.getName()))
                    .sorted(comparing(Dimension::getId))
                    .collect(toList());
        }

        private String getCacheKey(String term) {
            return "Dimension" + source.get() + database.get() + flowRef.get() + provider.getLanguages();
        }
    }

    @lombok.AllArgsConstructor
    private static final class AttributeCompletion<S extends Source> extends SdmxAutoCompletion {

        private final @NonNull HasSdmxProperties<? extends SdmxManager<S>> provider;

        private final @NonNull Supplier<S> source;

        private final @NonNull Supplier<DatabaseRef> database;

        private final @NonNull Supplier<FlowRef> flowRef;

        private final @NonNull ConcurrentMap<Object, Object> cache;

        @Override
        public @NonNull AutoCompletionSource getSource() {
            return ExtAutoCompletionSource.builder(this::load)
                    .behavior(this::getBehavior)
                    .postProcessor(this::filterAndSort)
                    .valueToString(Attribute::getId)
                    .cache(cache, this::getCacheKey, SYNC)
                    .build();
        }

        @Override
        public @NonNull ListCellRenderer<?> getRenderer() {
            return CustomListCellRenderer.of(Attribute::getId, Attribute::getName);
        }

        private List<Attribute> load(String term) throws Exception {
            return new ArrayList<>(provider.getSdmxManager()
                    .using(source.get())
                    .getMeta(FlowRequest.builder()
                            .languages(provider.getLanguages())
                            .database(database.get())
                            .flow(flowRef.get())
                            .build())
                    .getStructure()
                    .getAttributes());
        }

        private AutoCompletionSource.Behavior getBehavior(String term) {
            return source.get() != null && flowRef.get() != null ? ASYNC : NONE;
        }

        private List<Attribute> filterAndSort(List<Attribute> values, String term) {
            Predicate<String> filter = ExtAutoCompletionSource.basicFilter(term);
            return values.stream()
                    .filter(o -> filter.test(o.getId()) || filter.test(o.getName()))
                    .sorted(comparing(Attribute::getId))
                    .collect(toList());
        }

        private String getCacheKey(String term) {
            return "Attribute" + source.get() + database.get() + flowRef.get() + provider.getLanguages();
        }
    }
}
