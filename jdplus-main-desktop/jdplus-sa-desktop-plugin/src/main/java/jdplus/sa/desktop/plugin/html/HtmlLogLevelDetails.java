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
import java.util.Formatter;
import java.util.Locale;
import jdplus.sa.base.core.regarima.LogLevelModule;
import jdplus.toolkit.desktop.plugin.html.HtmlStream;
import jdplus.toolkit.desktop.plugin.html.core.HtmlLogFormatter;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(HtmlLogFormatter.class)
public class HtmlLogLevelDetails implements HtmlLogFormatter<LogLevelModule.Info> {

    @Override
    public Class getSourceClass() {
        return LogLevelModule.Info.class;
     }

    @Override
    public void write(HtmlStream stream, LogLevelModule.Info details, boolean verbose) throws IOException {
        
        stream.write("AICC on logs=").write(new Formatter(Locale.ROOT).format("%6g", details.getLogs()).toString());
        stream.newLine();
        stream.write("AICC on levels=").write(new Formatter(Locale.ROOT).format("%6g", details.getLevels()).toString());
        stream.newLine();
        stream.write("(AICC-preference)=").write(new Formatter(Locale.ROOT).format("%6g", details.getLogpreference()).toString()).write(')');
        
    }
    
}
