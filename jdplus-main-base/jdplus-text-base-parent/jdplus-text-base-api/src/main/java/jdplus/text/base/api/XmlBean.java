package jdplus.text.base.api;

import jdplus.toolkit.base.tsp.FileBean;

import java.io.File;
import java.nio.charset.Charset;

@lombok.Data
public final class XmlBean implements FileBean {

    private File file;

    private Charset charset;
}
