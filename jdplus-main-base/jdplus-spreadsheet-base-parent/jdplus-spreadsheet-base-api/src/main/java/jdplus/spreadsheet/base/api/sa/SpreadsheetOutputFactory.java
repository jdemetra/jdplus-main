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

package jdplus.spreadsheet.base.api.sa;

import jdplus.sa.base.api.SaOutputFactory;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;

/**
 * @author Kristof Bayens
 */
@DirectImpl
@ServiceProvider
public final class SpreadsheetOutputFactory implements SaOutputFactory {

    public static final String NAME = "Excel";
    private final SpreadsheetOutputConfiguration configuration;
    private boolean enabled = true;

    public SpreadsheetOutputFactory() {
        configuration = new SpreadsheetOutputConfiguration();
    }

    public SpreadsheetOutputFactory(SpreadsheetOutputConfiguration config) {
        configuration = config;
    }

    public SpreadsheetOutputConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public String getName() {
        return NAME;
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
    public SpreadsheetOutput create() {
        return new SpreadsheetOutput(configuration);
    }
}
