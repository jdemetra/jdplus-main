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
package jdplus.tramoseats.desktop.plugin.anomalydetection.html;

import jdplus.tramoseats.desktop.plugin.anomalydetection.AnomalyItem;
import jdplus.tramoseats.desktop.plugin.anomalydetection.OutlierEstimation;
import jdplus.toolkit.desktop.plugin.html.AbstractHtmlElement;
import jdplus.toolkit.desktop.plugin.html.HtmlElement;
import jdplus.toolkit.desktop.plugin.html.HtmlStream;
import jdplus.toolkit.desktop.plugin.html.HtmlTag;
import jdplus.toolkit.desktop.plugin.html.modelling.HtmlRegSarima;
import java.io.IOException;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;

/**
 * Html report of a check last processed Ts
 *
 * @author Mats Maggi
 */
public class HtmlCheckLast extends AbstractHtmlElement implements HtmlElement {

    private final AnomalyItem anomalyItem;
    private final RegSarimaModel model;
 
    public HtmlCheckLast(AnomalyItem a, RegSarimaModel m) {
        anomalyItem = a;
        model = m;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER1, anomalyItem.getName()).newLine();       

        HtmlRegSarima reg = new HtmlRegSarima(model, false);
        reg.writeDetails(stream, false);
        stream.write(new HtmlOutliers(OutlierEstimation.of(model)));
    }
}
