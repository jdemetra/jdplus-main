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
package jdplus.cruncher;

import static jdplus.cruncher.ArgsDecoder2.decode;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import static org.assertj.core.api.Assertions.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import picocli.CommandLine;

/**
 *
 * @author Philippe Charles
 */
public class ArgsDecoderTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void testNoArgs() {
        assertThat(decode()).isNull();
    }

    @Test
    @SuppressWarnings("null")
    public void testX() throws IOException {
        File userDir = temp.newFolder("X");

        assertThatThrownBy(() -> decode("workspace.xml", "-x", userDir.getAbsolutePath()))
                .as("Not a file")
                .isInstanceOf(CommandLine.ExecutionException.class)
                .hasCauseInstanceOf(AccessDeniedException.class);

        File configFile = new File(userDir, "config.xml");

        assertThatThrownBy(() -> decode("workspace.xml", "-x", configFile.getAbsolutePath()))
                .as("Missing file")
                .isInstanceOf(CommandLine.ExecutionException.class)
                .hasCauseInstanceOf(NoSuchFileException.class);

        write(configFile, "");

        assertThatThrownBy(() -> decode("workspace.xml", "-x", configFile.getAbsolutePath()))
                .as("Empty file")
                .isInstanceOf(CommandLine.ExecutionException.class)
                .hasCauseInstanceOf(NoSuchFileException.class);

        write(configFile, "<?xml version=\"1.0\" encoding=\"UTF-");

        assertThatThrownBy(() -> decode("workspace.xml", "-x", configFile.getAbsolutePath()))
                .as("Partial file")
                .isInstanceOf(CommandLine.ExecutionException.class)
                .hasCauseInstanceOf(IOException.class);

        write(configFile, "some invalid content");

        assertThatThrownBy(() -> decode("workspace.xml", "-x", configFile.getAbsolutePath()))
                .as("Invalid file")
                .isInstanceOf(CommandLine.ExecutionException.class)
                .hasCauseInstanceOf(IOException.class);

        WsaConfig config = new WsaConfig();
        WsaConfig.write(configFile, config);

        File workspace = new File(userDir, "workspace.xml");

        assertThat(decode("-x", configFile.getAbsolutePath(), workspace.getAbsolutePath()))
                .as("Valid args")
                .satisfies(o -> {
                    assertThat(o.getWorkspace()).isEqualTo(workspace);
                    assertThat(o.getConfig()).isEqualToComparingFieldByField(config);
                });
    }

    private static void write(File file, String content) throws IOException {
//        Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
//        Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
    }
}
