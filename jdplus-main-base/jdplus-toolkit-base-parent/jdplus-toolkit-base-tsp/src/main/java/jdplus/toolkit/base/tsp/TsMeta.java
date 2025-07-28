/*
 * Copyright 2018 National Bank of Belgium
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
package jdplus.toolkit.base.tsp;

import internal.toolkit.base.tsp.DefaultTsMeta;
import jdplus.toolkit.base.tsp.fixme.Strings;
import jdplus.toolkit.base.tsp.util.ObsFormat;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @param <T>
 * @author Philippe Charles
 */
public interface TsMeta<T> {

    @NonNull
    String getKey();

    @Nullable
    T load(@NonNull Function<String, String> meta);

    @Nullable
    default T load(@NonNull Map<String, String> meta) {
        return load(meta::get);
    }

    void store(@NonNull BiConsumer<String, String> meta, @NonNull T value);

    default void store(@NonNull Map<String, String> meta, @NonNull T value) {
        store(meta::put, value);
    }

    @NonNull
    static <T> TsMeta<T> on(@NonNull String key, @NonNull Parser<T> parser, @NonNull Formatter<T> formatter) {
        return new DefaultTsMeta<>(key, parser, formatter);
    }

    @NonNull
    static TsMeta<String> onString(@NonNull String key) {
        return new DefaultTsMeta<>(key, Parser.onString(), Formatter.onString());
    }

    @NonNull
    static TsMeta<LocalDateTime> onDateTime(@NonNull String key, @NonNull String datePattern, @NonNull Locale locale) {
        ObsFormat obsFormat = ObsFormat.builder().locale(locale).dateTimePattern(datePattern).build();
        return new DefaultTsMeta<>(key, obsFormat.dateTimeParser(), obsFormat.dateTimeFormatter());
    }

    @NonNull
    static TsMeta<LocalDateTime> onTimestamp() {
        DateTimeFormatter main = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        ObsFormat legacy = ObsFormat.builder().dateTimePattern("EEE MMM dd HH:mm:ss zzz yyyy").build();
        return new DefaultTsMeta<>("@timestamp",
                Parser.onDateTimeFormatter(main, LocalDateTime::from).orElse(legacy.dateTimeParser()),
                Formatter.onDateTimeFormatter(main));
    }

    // from ec.tss.Ts
    @Deprecated
    TsMeta<String> SOURCE_OLD = onString("tsmoniker.source");

    // from ec.tss.Ts
    @Deprecated
    TsMeta<String> ID_OLD = onString("tsmoniker.id");

    // from ec.tss.Ts
    @Deprecated
    TsMeta<String> DYNAMIC = onString("dynamic");

    // from ec.tss.Ts
    TsMeta<LocalDateTime> BEG = onDateTime("@beg", "yyyy-MM-dd", Locale.ROOT);

    // from ec.tss.Ts
    TsMeta<LocalDateTime> END = onDateTime("@end", "yyyy-MM-dd", Locale.ROOT);

    // from ec.tss.Ts
    TsMeta<?> CONFIDENTIAL = onString("@confidential");

    // from ec.tstoolkit.MetaData
    TsMeta<String> DESCRIPTION = onString("@description");

    // from ec.tstoolkit.MetaData
    TsMeta<String> OWNER = onString("@owner");

    // from ec.tstoolkit.MetaData
    TsMeta<String> SOURCE = onString("@source");

    // from ec.tstoolkit.MetaData
    TsMeta<String> ID = onString("@id");

    // from ec.tstoolkit.MetaData
    TsMeta<LocalDateTime> TIMESTAMP = onTimestamp();

    // from ec.tstoolkit.MetaData
    TsMeta<?> DOCUMENT = onString("@document");

    // from ec.tstoolkit.MetaData
    TsMeta<?> SUMMARY = onString("@summary");

    // from ec.tstoolkit.MetaData
    TsMeta<String> NOTE = onString("@note");

    // from ec.tstoolkit.MetaData
    TsMeta<?> TODO = onString("@todo");

    // from ec.tstoolkit.MetaData
    TsMeta<?> ALGORITHM = onString("@algorithm");

    // from ec.tstoolkit.MetaData
    TsMeta<?> QUALITY = onString("@quality");
}
