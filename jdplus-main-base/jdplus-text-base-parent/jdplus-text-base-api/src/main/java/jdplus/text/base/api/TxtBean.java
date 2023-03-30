package jdplus.text.base.api;

import jdplus.toolkit.base.api.timeseries.util.ObsGathering;
import jdplus.toolkit.base.tsp.FileBean;
import jdplus.toolkit.base.tsp.util.ObsFormat;

import java.io.File;
import java.nio.charset.Charset;

@lombok.Data
public final class TxtBean implements FileBean {

    public enum Delimiter {TAB, SEMICOLON, COMMA, SPACE}

    public enum TextQualifier {NONE, QUOTE, DOUBLE_QUOTE}

    File file;

    Charset charset;

    ObsFormat format;

    ObsGathering gathering;

    Delimiter delimiter;

    TextQualifier textQualifier;

    boolean headers;

    int skipLines;
}
