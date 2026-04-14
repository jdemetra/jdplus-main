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

import jdplus.sa.base.api.SaDocument;
import jdplus.sa.base.api.SaOutputFactory;
import jdplus.toolkit.base.api.processing.Output;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Kristof Bayens
 */
@ServiceProvider(SaOutputFactory.class)
public class CsvMatrixOutputFactory implements SaOutputFactory {

    public static final String NAME = "Csv matrix";

    private final CsvMatrixOutputConfiguration configuration;
    private volatile boolean enabled = true;

    public CsvMatrixOutputFactory() {
        configuration = new CsvMatrixOutputConfiguration();
    }

    public CsvMatrixOutputFactory(CsvMatrixOutputConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public CsvMatrixOutputConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public String getName() {
        return NAME;
    }

    //    @Override
//    public String getDescription() {
//        return "Csv matrix output";
//    }
//
    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public Output<SaDocument> create() {
        return new CsvMatrixOutput(configuration);
    }
}
