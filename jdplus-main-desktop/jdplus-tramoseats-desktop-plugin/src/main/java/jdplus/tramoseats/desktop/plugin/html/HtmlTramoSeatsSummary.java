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
package jdplus.tramoseats.desktop.plugin.html;

import jdplus.toolkit.desktop.plugin.html.AbstractHtmlElement;
import jdplus.toolkit.desktop.plugin.html.HtmlElement;
import jdplus.toolkit.desktop.plugin.html.HtmlStream;
import jdplus.toolkit.desktop.plugin.html.HtmlTag;
import jdplus.toolkit.desktop.plugin.html.core.HtmlProcessingLog;
import jdplus.toolkit.desktop.plugin.html.modelling.HtmlRegSarima;
import jdplus.toolkit.desktop.plugin.html.modelling.HtmlUcarima;
import jdplus.toolkit.desktop.plugin.html.core.HtmlDiagnosticsSummary;
import jdplus.toolkit.base.api.processing.ProcDiagnostic;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jdplus.toolkit.base.core.arima.ArimaModel;
import jdplus.toolkit.base.core.arima.IArimaModel;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.tramoseats.base.core.seats.SeatsResults;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsFactory;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsResults;
import jdplus.toolkit.base.core.ucarima.UcarimaModel;

/**
 *
 * @author Kristof Bayens
 */
public class HtmlTramoSeatsSummary extends AbstractHtmlElement implements HtmlElement {

    private final ProcessingLog infos_;
    private final RegSarimaModel preprocessing_;
    private final SeatsResults decomposition_;
    private final String[] names_;
    private final ArimaModel[] list_;
    private final List<ProcDiagnostic> diags_=new ArrayList<>();
    private final String title_;

    public HtmlTramoSeatsSummary(String title, TramoSeatsResults results, String[] names, ArimaModel[] list) {
        title_ = title;
        preprocessing_ = results.getPreprocessing();
        decomposition_ = results.getDecomposition();
        names_ = names;
        list_ = list;
        TramoSeatsFactory.getInstance().fillDiagnostics(diags_, null, results);
        infos_=results.getLog();
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        writeTitle(stream);
        writeInformation(stream);
        if (preprocessing_ == null && decomposition_ == null)
            return;
        writePreprocessing(stream);
        writeDecomposition(stream);
        writeDiagnostics(stream);
    }

    private void writeTitle(HtmlStream stream) throws IOException {
        if (title_ != null) {
            stream.write(HtmlTag.HEADER1, title_).newLine();
        }
    }

    private void writeInformation(HtmlStream stream) throws IOException {
//        HtmlProcessingLog log = new HtmlProcessingLog(infos_);
//        log.setVerbose(false);
//        stream.write(log);
    }

    private void writePreprocessing(HtmlStream stream) throws IOException {
        if (preprocessing_ == null)
            return;
        stream.write(HtmlTag.HEADER2, "Pre-processing (Tramo)").newLine();
        stream.write(new HtmlRegSarima(preprocessing_, true));
    }

    private void writeDecomposition(HtmlStream stream) throws IOException {
        if (decomposition_ == null)
            return;
        stream.write(HtmlTag.HEADER2, "Decomposition (Seats)").newLine();
        SarimaModel tmodel = preprocessing_.arima();
        IArimaModel smodel = decomposition_.getUcarimaModel().getModel();
        if (tmodel == null || smodel == null) {
            stream.write(HtmlTag.IMPORTANT_TEXT, "No decomposition").newLine();
        } else {
            boolean changed = !ArimaModel.same(tmodel, smodel, 1e-4);
            if (changed) {
                stream.write(HtmlTag.IMPORTANT_TEXT, "Model changed by Seats").newLine();
            }

            UcarimaModel ucm = decomposition_.getUcarimaModel();
            HtmlUcarima arima = new HtmlUcarima(ucm.getModel(), list_, names_);
            arima.writeSummary(stream);
            stream.newLine();
        }
    }

    private void writeDiagnostics(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, "Diagnostics").newLine();
        stream.write(new HtmlDiagnosticsSummary(diags_));
    }
}
