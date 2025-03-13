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
package jdplus.toolkit.desktop.plugin.html.core;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jdplus.toolkit.desktop.plugin.html.HtmlStream;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class HtmlLogFormatters {

    volatile Map<Class, HtmlLogFormatter> extractors;

    public void reloadExtractors() {
        List<HtmlLogFormatter> load = HtmlLogFormatterLoader.load();
        HashMap<Class, HtmlLogFormatter> x = new HashMap<>();
        load.forEach(cur -> {
            x.put(cur.getSourceClass(), cur);
        });
        extractors = Collections.unmodifiableMap(x);
    }

    public <D> HtmlLogFormatter<D> extractor(Class D) {
        if (extractors == null) {
            reloadExtractors();
        }
        return (HtmlLogFormatter<D>) extractors.get(D);
    }

    public void write(HtmlStream stream, Object details, boolean verbose) throws IOException {
        if (details == null)
            return;
        HtmlLogFormatter fmt = extractor(details.getClass());
        if (fmt != null) {
            fmt.write(stream, details, verbose);
        }else{
            stream.write(details.toString()).newLine();
        }
    }
}
