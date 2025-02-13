/*
 * Copyright 2016 National Bank of Belgium
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
package tck.demetra.data;

import jdplus.toolkit.base.api.math.matrices.Matrix;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author Jean Palate
 */
public class MatrixSerializer {

    public static final double ND = -99999;

    public static Matrix read(File file, String separators) throws FileNotFoundException, IOException {
        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            return read(reader, Locale.ROOT, separators);
        }
    }

    public static Matrix read(File file) throws FileNotFoundException, IOException {
        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            return read(reader, Locale.ROOT, "\\s+|,");
        }
    }

    public static Matrix read(InputStream stream, String separators) throws FileNotFoundException, IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return read(reader, Locale.ROOT, separators);
        }
    }

    public static Matrix read(InputStream stream) throws FileNotFoundException, IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return read(reader, Locale.ROOT, "\\s+|,");
        }
    }

    public static void write(Matrix m, File file) throws FileNotFoundException, IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write(m.toString());
        }
    }

    public static Matrix read(BufferedReader reader, Locale locale, String separators) throws FileNotFoundException, IOException {

        List<double[]> data = new ArrayList<>();
        String curline;
        int nc = 0;
        while ((curline = reader.readLine()) != null) {
            double[] c = split(curline, locale, separators);
            if (c != null) {
                if (nc < c.length) {
                    nc = c.length;
                }
                data.add(c);
            }
        }
        if (data.isEmpty()) {
            return null;
        }
        int nrows = data.size();
        double[] all = new double[nrows * nc];
        for (int i = 0; i < nrows; ++i) {
            double[] cur = data.get(i);
            if (cur == null) {
                for (int j = 0; j < nc; ++j) {
                    all[i + j * nrows] = Double.NaN;
                }
            } else {
                for (int j = 0; j < cur.length; ++j) {
                    all[i + j * nrows] = cur[j];
                }
                for (int j = cur.length; j < nc; ++j) {
                    all[i + j * nrows] = Double.NaN;
                }
            }
        }
        return Matrix.of(all, nrows, nc);

    }

    private static double[] split(String line, Locale locale, String separators) {
        String[] items = line.split(separators);
        double[] data = new double[items.length];
        NumberFormat fmt = NumberFormat.getNumberInstance(locale);
        for (int i = 0; i < data.length; ++i) {
            try {
                Number n = fmt.parse(items[i]);
                data[i] = n.doubleValue();
            } catch (ParseException ex) {
                data[i] = Double.NaN;
            }
        }
        for (int i = 0; i < data.length; ++i) {
            if (data[i] == ND) {
                data[i] = Double.NaN;
            }
        }
        return data;
    }

}
