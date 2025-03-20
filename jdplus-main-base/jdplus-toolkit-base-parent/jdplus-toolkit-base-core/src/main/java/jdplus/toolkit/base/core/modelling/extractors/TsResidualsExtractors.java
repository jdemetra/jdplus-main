/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.toolkit.base.core.modelling.extractors;

import jdplus.toolkit.base.api.information.DynamicMapping;
import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.toolkit.base.api.stats.StatisticalTest;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.dictionaries.ResidualsDictionaries;
import jdplus.toolkit.base.api.timeseries.TsResiduals;
import nbbrd.design.Development;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@lombok.experimental.UtilityClass
public class TsResidualsExtractors {

    @ServiceProvider(InformationExtractor.class)
    public static class Specific extends InformationMapping<TsResiduals> {

        public Specific() {
            set(ResidualsDictionaries.TYPE, String.class, source -> source.getType().name());
            set(ResidualsDictionaries.RES, double[].class, source -> source.getRes().toArray());
            set(ResidualsDictionaries.TSRES, TsData.class, source -> source.getTsres());
            set(ResidualsDictionaries.N, Integer.class, source -> source.getN());
            set(ResidualsDictionaries.DF, Integer.class, source -> source.getDf());
            set(ResidualsDictionaries.DFC, Integer.class, source -> source.getDfc());
            set(ResidualsDictionaries.SER, Double.class, source
                    -> {
                int df = source.getDfc();
                return df == 0 ? Double.NaN : Math.sqrt(source.getSsq() / df);
            });
            set(ResidualsDictionaries.SER_ML, Double.class, source
                    -> {
                int df = source.getN();
                return df == 0 ? Double.NaN : Math.sqrt(source.getSsq() / df);
            });
            addTest(ResidualsDictionaries.MEAN);
            addTest(ResidualsDictionaries.DH);
            addTest(ResidualsDictionaries.SKEW);
            addTest(ResidualsDictionaries.KURT);
            addTest(ResidualsDictionaries.LB);
            addTest(ResidualsDictionaries.BP);
            addTest(ResidualsDictionaries.SEASLB);
            addTest(ResidualsDictionaries.SEASBP);
            addTest(ResidualsDictionaries.LB2);
            addTest(ResidualsDictionaries.BP2);
            addTest(ResidualsDictionaries.NRUNS);
            addTest(ResidualsDictionaries.LRUNS);
            addTest(ResidualsDictionaries.NUDRUNS);
            addTest(ResidualsDictionaries.LUDRUNS);
        }

        private void addTest(String k) {
            set(k, StatisticalTest.class, source -> source.getTests().get(k));
        }

        @Override
        public Class<TsResiduals> getSourceClass() {
            return TsResiduals.class;
        }

        @Override
        public int getPriority() {
            return 1;
        }
    }

    @ServiceProvider(InformationExtractor.class)
    public static class Dynamic extends DynamicMapping<TsResiduals, StatisticalTest> {

        public Dynamic() {
            super(null, v -> v.getTests());
        }

        @Override
        public Class<TsResiduals> getSourceClass() {
            return TsResiduals.class;
        }

        @Override
        public int getPriority() {
            return 0;
        }
    }

}
