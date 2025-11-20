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
import jdplus.toolkit.base.core.regsarima.regular.IOutliersDetectionModule;
import jdplus.toolkit.desktop.plugin.html.HtmlStream;
import jdplus.toolkit.desktop.plugin.html.HtmlTable;
import jdplus.toolkit.desktop.plugin.html.HtmlTableCell;
import jdplus.toolkit.desktop.plugin.html.HtmlTag;
import jdplus.toolkit.desktop.plugin.html.core.HtmlLogFormatter;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(HtmlLogFormatter.class)
public class HtmlOutliersSelection implements HtmlLogFormatter<IOutliersDetectionModule.Info> {

    static final DecimalFormat DF2 = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.getDefault(Locale.Category.FORMAT)));

    @Override
    public Class<IOutliersDetectionModule.Info> getSourceClass() {
        return IOutliersDetectionModule.Info.class;
    }

    @Override
    public void write(HtmlStream stream, IOutliersDetectionModule.Info details, boolean verbose) throws IOException {
        stream.write("critical value :");
        stream.write(DF2.format(details.getVa())).newLine();
        int[][] outliers = details.getOutliers();
        if (outliers.length > 0) {
            stream.open(new HtmlTable().withWidth(200));
            String[] types = details.getTypes();
            for (int[] o : outliers) {
                stream.open(HtmlTag.TABLEROW);
                stream.write(new HtmlTableCell(types[o[1]]).withWidth(100));
                stream.write(new HtmlTableCell(Integer.toString(o[0])).withWidth(100));
                stream.close(HtmlTag.TABLEROW);
            }
            stream.close(HtmlTag.TABLE);
        }

    }

}
