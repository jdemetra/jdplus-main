/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.tramoseats.base.workspace;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import jdplus.toolkit.base.workspace.file.util.InformationSetSupport;
import jdplus.tramoseats.base.api.tramo.TramoSpec;
import jdplus.tramoseats.base.api.tramoseats.TramoSeatsSpec;
import jdplus.tramoseats.base.information.TramoSeatsSpecMapping;
import jdplus.tramoseats.base.information.TramoSpecMapping;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class Utility {

    public TramoSpec readTramoSpec(String sfile) throws IOException {
        File file = Path.of(sfile).toFile();
        return InformationSetSupport.readItem(file.toPath(), TramoSpecMapping.SERIALIZER_V3);
    }

    public void writeTramoSpec(TramoSpec spec, String sfile) throws IOException {
        File file = Path.of(sfile).toFile();
        InformationSetSupport.writeItem(file.toPath(), TramoSpecMapping.SERIALIZER_V3, spec);
    }

    public TramoSeatsSpec readTramoSeatsSpec(String sfile) throws IOException {
        File file = Path.of(sfile).toFile();
        return InformationSetSupport.readItem(file.toPath(), TramoSeatsSpecMapping.SERIALIZER_V3);
    }

    public void writeTramoSeatsSpec(TramoSeatsSpec spec, String sfile) throws IOException {
        File file = Path.of(sfile).toFile();
        InformationSetSupport.writeItem(file.toPath(), TramoSeatsSpecMapping.SERIALIZER_V3, spec);
    }

}
