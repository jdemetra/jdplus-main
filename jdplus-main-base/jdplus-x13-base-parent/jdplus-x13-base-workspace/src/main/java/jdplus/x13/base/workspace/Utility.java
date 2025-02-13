/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.x13.base.workspace;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import jdplus.toolkit.base.workspace.file.util.InformationSetSupport;
import jdplus.x13.base.api.regarima.RegArimaSpec;
import jdplus.x13.base.api.x13.X13Spec;
import jdplus.x13.base.information.RegArimaSpecMapping;
import jdplus.x13.base.information.X13SpecMapping;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class Utility {

    public RegArimaSpec readRegArimaSpec(String sfile) throws IOException {
        File file = Path.of(sfile).toFile();
        return InformationSetSupport.readItem(file.toPath(), RegArimaSpecMapping.SERIALIZER_V3);
    }
    
    public void writeRegArimaSpec(RegArimaSpec spec, String sfile) throws IOException {
        File file = Path.of(sfile).toFile();
        InformationSetSupport.writeItem(file.toPath(), RegArimaSpecMapping.SERIALIZER_V3, spec);
    }

    public X13Spec readX13Spec(String sfile) throws IOException {
        File file = Path.of(sfile).toFile();
        return InformationSetSupport.readItem(file.toPath(), X13SpecMapping.SERIALIZER_V3);
    }
    
    public void writeX13Spec(X13Spec spec, String sfile) throws IOException {
        File file = Path.of(sfile).toFile();
        InformationSetSupport.writeItem(file.toPath(), X13SpecMapping.SERIALIZER_V3, spec);
    }

}
