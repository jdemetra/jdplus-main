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
package jdplus.x13.desktop.plugin.html;

import jdplus.toolkit.desktop.plugin.html.AbstractHtmlElement;
import jdplus.toolkit.desktop.plugin.html.HtmlStream;
import jdplus.toolkit.desktop.plugin.html.HtmlTag;
import jdplus.toolkit.desktop.plugin.html.core.HtmlDiagnosticsSummary;
import jdplus.toolkit.desktop.plugin.html.core.HtmlProcessingLog;
import jdplus.toolkit.desktop.plugin.html.modelling.HtmlRegSarima;
import jdplus.toolkit.base.api.processing.ProcDiagnostic;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import jdplus.x13.base.core.x11.X11Results;
import jdplus.x13.base.core.x13.X13Factory;
import jdplus.x13.base.core.x13.X13Results;

/**
 *
 * @author Kristof Bayens
 */
public class HtmlX13Summary extends AbstractHtmlElement {

    private final ProcessingLog infos_;
    private final RegSarimaModel preprocessing_;
    private final X11Results decomposition_;
    private final List<ProcDiagnostic> diags_ = new ArrayList<>();
    private final String title_;

    public HtmlX13Summary(String title, X13Results results) {
        title_ = title;
        preprocessing_ = results.getPreprocessing();
        decomposition_ = results.getDecomposition();
        X13Factory.getInstance().fillDiagnostics(diags_, null, results);
        infos_ = results.getLog();
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        writeTitle(stream);
        writeInformation(stream);
        if (preprocessing_ == null && decomposition_ == null) {
            return;
        }
        writePreprocessing(stream);
        writeDiagnostics(stream);
    }

    private void writeTitle(HtmlStream stream) throws IOException {
        if (title_ != null) {
            stream.write(HtmlTag.HEADER1, title_).newLine();
        }
    }

    private void writeInformation(HtmlStream stream) throws IOException {
//        stream.write(new HtmlProcessingLog(infos_));
    }

    private void writePreprocessing(HtmlStream stream) throws IOException {
        if (preprocessing_ == null) {
            stream.write(HtmlTag.HEADER2, "No pre-processing").newLine();
        } else {
            stream.write(HtmlTag.HEADER2, "Pre-processing (RegArima)").newLine();
            stream.write(new HtmlRegSarima(preprocessing_, true));
        }
    }

    private void writeDecomposition(HtmlStream stream) throws IOException {
    }

    private void writeDiagnostics(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, "Diagnostics").newLine();
        stream.write(new HtmlDiagnosticsSummary(diags_));
    }
}
