/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
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
