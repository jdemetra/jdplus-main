/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
package jdplus.toolkit.base.tsp.legacy;

import jdplus.toolkit.base.api.design.DemetraPlusLegacy;
import jdplus.toolkit.base.tsp.DataSource;
import java.io.File;
import java.nio.file.Path;

import nbbrd.io.text.Parser;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;

/**
 *
 * @author Demortier Jeremy
 */
@DemetraPlusLegacy
@lombok.Value
public class LegacyFileId implements CharSequence {

    @Nullable
    public static LegacyFileId of(@NonNull File file) {
        return parse(file.getAbsolutePath());
    }

    @Nullable
    public static LegacyFileId parse(@NonNull CharSequence input) {
        return input instanceof LegacyFileId ? (LegacyFileId) input : parse(input.toString());
    }

    @Nullable
    public static LegacyFileId parse(@NonNull String input) {
        if (!isDemetraUri(input)) {
            LegacyId id = LegacyId.parse(LegacyId.Handler.PLAIN, input);
            if (id != null && id.getCount() == 1 && isValidPath(Path.of(id.get(0)).toFile())) {
                return new LegacyFileId(id.get(0));
            }
        }
        return null;
    }

    private final String file;

    private LegacyFileId(String file) {
        this.file = file;
    }

    @Override
    public String toString() {
        return file;
    }

    @Override
    public int length() {
        return file.length();
    }

    @Override
    public char charAt(int index) {
        return file.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return file.subSequence(start, end);
    }

    public DataSource toDataSource(@NonNull String providerName, @NonNull String version) {
        return DataSource.of(providerName, version, "file", file);
    }

    @NonNull
    public static Parser<DataSource> asDataSourceParser(@NonNull String providerName, @NonNull String version) {
        return o -> getDataSource(o, providerName, version);
    }

    private static boolean isDemetraUri(String input) {
        return input.startsWith("demetra://");
    }

    private static DataSource getDataSource(CharSequence o, String providerName, String version) {
        LegacyFileId id = LegacyFileId.parse(o);
        return id != null ? id.toDataSource(providerName, version) : null;
    }

    /**
     *
     * @param file
     * @return
     * @deprecated may return false positives!
     */
    @Deprecated
    private static boolean isValidPath(File file) {
        try {
            file.getCanonicalPath();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
