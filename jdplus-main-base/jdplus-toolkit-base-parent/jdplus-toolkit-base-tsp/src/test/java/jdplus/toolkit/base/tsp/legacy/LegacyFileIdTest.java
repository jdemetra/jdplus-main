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

import jdplus.toolkit.base.tsp.DataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Philippe Charles
 */
public class LegacyFileIdTest {

    static File FILE, OTHER;

    @BeforeAll
    public static void beforeClass() throws IOException {
        FILE = File.createTempFile("123", "456");
        OTHER = File.createTempFile("aaa", "bbb");
    }

    @AfterAll
    public static void afterClass() {
        FILE.delete();
        OTHER.delete();
    }

    @Test
    public void testDemetraUri() {
        String input = DataSource.of("p", "123").toString();
        assertThat(LegacyFileId.parse(input)).isNull();
    }

    @Test
    public void testFromFile() {
        LegacyFileId sourceId = LegacyFileId.of(FILE);
        assertNotNull(sourceId);
        assertEquals(FILE, Path.of(sourceId.getFile()).toFile());
    }

    @Test
    public void testParseString() {
        LegacyFileId sourceId = LegacyFileId.parse(FILE.getPath());
        assertNotNull(sourceId);
        assertEquals(FILE, Path.of(sourceId.getFile()).toFile());
    }

    @Test
    public void testParseCharSequence() {
        LegacyFileId sourceId = LegacyFileId.parse((CharSequence) FILE.getPath());
        assertNotNull(sourceId);
        assertEquals(FILE, Path.of(sourceId.getFile()).toFile());
    }

    @Test
    public void testEquals() throws IOException {
        LegacyFileId sourceId = LegacyFileId.of(FILE);

        assertEquals(sourceId, LegacyFileId.of(FILE));
        assertNotSame(sourceId, LegacyFileId.of(FILE));
        assertNotEquals(sourceId, LegacyFileId.of(OTHER));

        assertEquals(sourceId, LegacyFileId.parse(FILE.getPath()));
        assertNotSame(sourceId, LegacyFileId.parse(FILE.getPath()));
        assertNotEquals(sourceId, LegacyFileId.parse(OTHER.getPath()));

        assertEquals(sourceId, LegacyFileId.parse((CharSequence) sourceId));
        assertSame(sourceId, LegacyFileId.parse((CharSequence) sourceId));
    }
}
