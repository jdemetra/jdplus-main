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
package jdplus.tramoseats.desktop.plugin.html;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Formatter;
import java.util.Locale;
import jdplus.toolkit.base.api.arima.SarmaOrders;
import jdplus.toolkit.desktop.plugin.html.AbstractHtmlElement;
import jdplus.toolkit.desktop.plugin.html.HtmlStream;
import jdplus.toolkit.desktop.plugin.html.HtmlTable;
import jdplus.toolkit.desktop.plugin.html.HtmlTableCell;
import jdplus.toolkit.desktop.plugin.html.HtmlTag;
import jdplus.toolkit.desktop.plugin.html.core.HtmlLogFormatter;
import jdplus.tramoseats.base.core.tramo.internal.ArmaModelSelector;
import jdplus.tramoseats.base.core.tramo.internal.ArmaModule;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(HtmlLogFormatter.class)
public class HtmlArmaSelection implements HtmlLogFormatter<ArmaModule.Info> {

    static final DecimalFormat DF4 = new DecimalFormat("0.0000", DecimalFormatSymbols.getInstance(Locale.getDefault(Locale.Category.FORMAT)));

    @Override
    public Class<ArmaModule.Info> getSourceClass() {
        return ArmaModule.Info.class;
    }

    @Override
    public void write(HtmlStream stream, ArmaModule.Info details, boolean verbose) throws IOException {
        
        stream.write("Selected model").write(details.getSelection().toString());
        
        stream.newLine();
        stream.open(new HtmlTable().withWidth(200));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("ARMA").withWidth(100));
        stream.write(new HtmlTableCell("BIC").withWidth(100));
        stream.close(HtmlTag.TABLEROW);

        for (ArmaModelSelector.FastBIC cur : details.getModels()) {
            SarmaOrders arma = cur.getArma().orders().doStationary();
            stream.open(HtmlTag.TABLEROW);

            stream.write(new HtmlTableCell(arma.toString()).withWidth(100));
            stream.write(new HtmlTableCell(DF4.format(cur.getBIC())).withWidth(100));
            stream.close(HtmlTag.TABLEROW);
        }
        stream.close(HtmlTag.TABLE);
        
    }

}
