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
package jdplus.sa.base.csv;

import jdplus.toolkit.base.api.information.Explorable;
import jdplus.toolkit.base.api.processing.Output;
import jdplus.sa.base.api.SaDocument;
import jdplus.toolkit.base.api.util.NamedObject;
import jdplus.toolkit.base.api.util.Paths;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kristof Bayens
 */
public class CsvMatrixOutput implements Output<SaDocument> {

    CsvMatrixOutputConfiguration config;
    List<NamedObject<Explorable>> infos;
    private File folder;
    private boolean fullName;

    public CsvMatrixOutput(CsvMatrixOutputConfiguration config) {
        this.config = (CsvMatrixOutputConfiguration) config.clone();
        this.fullName = this.config.isFullName();
    }

    @Override
    public String getName() {
        return "Csv matrix";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void start(Object context) {
        infos = new ArrayList<>();
        folder = Paths.folderFromContext(config.getFolder(), context);
    }

    @Override
    public void end(Object context) throws Exception {
        String file = Paths.concatenate(folder.getAbsolutePath(), config.getFileName());
        file = Paths.changeExtension(file, "csv");
        try (Writer writer = Files.newBufferedWriter(Path.of(file), StandardCharsets.ISO_8859_1)) {
            CsvInformationFormatter.formatResults(writer, infos, config.getItems(), config.isShortColumnName(), fullName);
        }
        infos = null;
    }

    @Override
    public void process(SaDocument document) {
        infos.add(new NamedObject<>(document.getName(), document.getResults()));
    }
}
