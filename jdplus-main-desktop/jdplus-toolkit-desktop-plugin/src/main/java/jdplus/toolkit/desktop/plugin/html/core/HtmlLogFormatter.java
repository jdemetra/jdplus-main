/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package jdplus.toolkit.desktop.plugin.html.core;

import java.io.IOException;
import jdplus.toolkit.desktop.plugin.html.HtmlStream;
import nbbrd.design.Development;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;

/**
 *
 * @author Jean Palate
 * @param <S>
 */
@ServiceDefinition(quantifier = Quantifier.MULTIPLE)
@Development(status = Development.Status.Release)
public interface HtmlLogFormatter<S> {

    Class<S> getSourceClass();
    void write(HtmlStream stream, S details, boolean verbose) throws IOException;
}
