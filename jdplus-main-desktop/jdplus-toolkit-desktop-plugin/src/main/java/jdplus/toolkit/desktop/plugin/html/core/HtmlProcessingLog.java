/*
 * Copyright 2013-2014 National Bank of Belgium
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
package jdplus.toolkit.desktop.plugin.html.core;

import jdplus.toolkit.desktop.plugin.html.AbstractHtmlElement;
import jdplus.toolkit.desktop.plugin.html.Bootstrap4;
import jdplus.toolkit.desktop.plugin.html.HtmlStream;
import jdplus.toolkit.desktop.plugin.html.HtmlTag;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Jean Palate
 */
public class HtmlProcessingLog extends AbstractHtmlElement {

    private final ProcessingLog infos_;
    private boolean err = true, wrn = true, rem = true, info = false, verbose = true;

    public HtmlProcessingLog(final ProcessingLog infos) {
        infos_ = infos;
    }

    public void displayErrors(boolean e) {
        err = e;
    }

    public void displayWarnings(boolean e) {
        wrn = e;
    }

    public void displayRemarks(boolean e) {
        rem = e;
    }

    public void displayInfos(boolean e) {
        info = e;
    }

    public void setVerbose(boolean v) {
        verbose = v;
    }

    public boolean isVerbose() {
        return verbose;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        List<ProcessingLog.Information> all = infos_.all();
        if (all.isEmpty()) {
            return;
        }
        if (err) {
            List<ProcessingLog.Information> list = all.stream().filter(log -> log.getType() == ProcessingLog.InformationType.Error).toList();
            if (!list.isEmpty()) {
                stream.write(HtmlTag.HEADER2, "Errors").newLine();
                list.forEach(msg -> {
                    try {
                        stream.write(HtmlTag.IMPORTANT_TEXT, msg.getMsg()).newLine();
                        if (verbose) {
                            stream.newLine();
                            Object details = msg.getDetails();
                            if (details != null) {
                                HtmlLogFormatters.write(stream, details, verbose);
                                stream.newLine();
                            }
                        }
                    } catch (IOException ex) {
                    }
                });
            }
        }
        if (wrn) {
            List<ProcessingLog.Information> list = all.stream().filter(log -> log.getType() == ProcessingLog.InformationType.Warning).toList();
            if (!list.isEmpty()) {
                stream.write(HtmlTag.HEADER2, "Warnings").newLine();
                list.forEach(msg -> {
                    try {
                        stream.write(HtmlTag.IMPORTANT_TEXT, msg.getMsg()).newLine();
                        if (verbose) {
                            stream.newLine();
                            Object details = msg.getDetails();
                            if (details != null) {
                                HtmlLogFormatters.write(stream, details, verbose);
                                stream.newLine();
                            }
                        }
                    } catch (IOException ex) {
                    }
                });
            }
        }
        if (rem) {
            List<ProcessingLog.Information> list = all.stream().filter(log -> log.getType() == ProcessingLog.InformationType.Remark).toList();
            if (!list.isEmpty()) {
                stream.write(HtmlTag.HEADER2, "Remarks").newLine();
                list.forEach(msg -> {
                    try {
                        stream.write(HtmlTag.IMPORTANT_TEXT, msg.getMsg()).newLine();
                        if (verbose) {
                            stream.newLine();
                            Object details = msg.getDetails();
                            if (details != null) {
                                HtmlLogFormatters.write(stream, details, verbose);
                                stream.newLine();
                            }
                        }
                    } catch (IOException ex) {
                    }
                });
            }
        }
        if (info) {
            List<ProcessingLog.Information> list = all.stream().filter(log -> log.getType() == ProcessingLog.InformationType.Info).toList();
            if (!list.isEmpty()) {
                stream.write(HtmlTag.HEADER2, "Infos").newLine();
                list.forEach(msg -> {
                    try {
                        stream.write(HtmlTag.IMPORTANT_TEXT, msg.getMsg()).newLine();
                        if (verbose) {
                            stream.newLine();
                            Object details = msg.getDetails();
                            if (details != null) {
                                HtmlLogFormatters.write(stream, details, verbose);
                                stream.newLine();
                            }
                        }
                    } catch (IOException ex) {
                    }
                });
            }
        }
    }

}
