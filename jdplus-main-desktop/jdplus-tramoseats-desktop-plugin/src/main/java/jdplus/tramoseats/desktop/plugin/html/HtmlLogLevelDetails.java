/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.tramoseats.desktop.plugin.html;

import java.io.IOException;
import java.util.Formatter;
import java.util.Locale;
import jdplus.toolkit.desktop.plugin.html.HtmlStream;
import jdplus.toolkit.desktop.plugin.html.core.HtmlLogFormatter;
import jdplus.tramoseats.base.core.tramo.LogLevelModule;
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
        
        stream.write("-LogLikelihood on logs=").write(new Formatter(Locale.ROOT).format("%6g", details.getLogs()).toString());
        stream.newLine();
        stream.write("-LogLikelihood on levels=").write(new Formatter(Locale.ROOT).format("%6g", details.getLevels()).toString());
        stream.newLine();
        stream.write("(log-preference)=").write(new Formatter(Locale.ROOT).format("%6g", details.getLogpreference()).toString()).write(')');
        
    }
    
}
