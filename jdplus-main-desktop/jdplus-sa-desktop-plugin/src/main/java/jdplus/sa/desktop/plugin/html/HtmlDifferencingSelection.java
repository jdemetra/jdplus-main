/*
 * Copyright 2025 JDemetra+.
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.sa.desktop.plugin.html;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import jdplus.toolkit.base.core.regsarima.regular.IDifferencingModule;
import jdplus.toolkit.desktop.plugin.html.HtmlStream;
import jdplus.toolkit.desktop.plugin.html.core.HtmlLogFormatter;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(HtmlLogFormatter.class)
public class HtmlDifferencingSelection implements HtmlLogFormatter<IDifferencingModule.Info> {

    static final DecimalFormat DF4 = new DecimalFormat("0.0000", DecimalFormatSymbols.getInstance(Locale.getDefault(Locale.Category.FORMAT)));

    @Override
    public Class<IDifferencingModule.Info> getSourceClass() {
        return IDifferencingModule.Info.class;
    }

    @Override
    public void write(HtmlStream stream, IDifferencingModule.Info details, boolean verbose) throws IOException {
        stream.write("D: ").write(Integer.toString(details.getD()));
        stream.newLine();
        stream.write("BD: ").write(Integer.toString(details.getBd()));
        stream.newLine();
        if (details.isMean()) {
            stream.write("mean selected: T=").write(DF4.format(details.getTmean()));
        } else {
            stream.write("no mean: T=").write(DF4.format(details.getTmean()));
        }

        stream.newLine();
    }

}
