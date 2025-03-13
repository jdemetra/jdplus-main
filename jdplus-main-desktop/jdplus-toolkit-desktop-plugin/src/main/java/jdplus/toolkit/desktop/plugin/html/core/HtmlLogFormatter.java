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
