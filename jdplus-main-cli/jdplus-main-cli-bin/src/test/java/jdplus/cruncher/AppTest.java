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
package jdplus.cruncher;

import jdplus.cruncher.App;
import jdplus.cruncher.WsaConfig;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit test for simple App.
 */
public class AppTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void testGenerateDefaultConfigFile() throws IOException {
        File userDir = temp.newFolder("NoArgs");

        App.generateDefaultConfigFile(userDir);

        assertThat(userDir.toPath().resolve(WsaConfig.DEFAULT_FILE_NAME).toFile())
                .hasContent(writeConfigToString(WsaConfig.generateDefault()));
    }

    private static String writeConfigToString(WsaConfig config) throws IOException {
        try {
            JAXBContext context = JAXBContext.newInstance(WsaConfig.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            StringWriter writer = new StringWriter();
            marshaller.marshal(config, writer);
            return writer.toString();
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }
    }
}
