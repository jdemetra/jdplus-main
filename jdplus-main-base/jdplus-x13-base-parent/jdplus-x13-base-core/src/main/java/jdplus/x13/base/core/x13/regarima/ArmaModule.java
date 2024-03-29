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
package jdplus.x13.base.core.x13.regarima;

import nbbrd.design.BuilderPattern;
import nbbrd.design.Development;
import jdplus.toolkit.base.core.regarima.RegArimaEstimation;
import jdplus.toolkit.base.core.regarima.RegArimaModel;
import jdplus.toolkit.base.core.sarima.SarimaModel;
import jdplus.toolkit.base.api.arima.SarimaOrders;
import jdplus.toolkit.base.api.arima.SarmaOrders;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.regarima.RegArimaUtility;
import jdplus.toolkit.base.core.regarima.IRegArimaComputer;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class ArmaModule {

    static final double NO_BIC = 99999;
    static final int NMOD = 5;

    static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(ArmaModule.class)
    static class Builder {

        private int modelsCount = NMOD;
        private boolean wn = false;
        private boolean balanced = false, mixed = true;
        private double eps = 1e-5;
        private int maxP = 2, maxBp = 1, maxQ = 2, maxBq = 1;

        private Builder() {
        }

        Builder balanced(boolean balanced) {
            this.balanced = balanced;
            return this;
        }

        Builder mixed(boolean mixed) {
            this.mixed = mixed;
            return this;
        }

        Builder estimationPrecision(double eps) {
            this.eps = eps;
            return this;
        }

        Builder modelsCount(int n) {
            this.modelsCount = n;
            return this;
        }

        Builder maxP(int p) {
            this.maxP = p;
            return this;
        }

        Builder maxBp(int bp) {
            this.maxBp = bp;
            return this;
        }

        Builder maxQ(int q) {
            this.maxQ = q;
            return this;
        }

        Builder maxBq(int bq) {
            this.maxBq = bq;
            return this;
        }

        Builder acceptWhiteNoise(boolean ok) {
            this.wn = ok;
            return this;
        }

        ArmaModule build() {
            return new ArmaModule(this);
        }
    }

    public SarmaOrders process(DoubleSeq data, int period, int d, int bd, boolean seas) {
        return select(data, period, d, bd);
    }

    public SarimaOrders process(RegArimaModel<SarimaModel> regarima, boolean seas) {
        SarimaOrders curSpec = regarima.arima().orders();
        DoubleSeq res = RegArimaUtility.olsResiduals(regarima);
        SarmaOrders nspec = select(res, curSpec.getPeriod(), curSpec.getD(), curSpec.getBd());
        if (nspec == null) {
            curSpec.setDefault(seas);
            return curSpec;
        } else {
            SarimaOrders rspec = SarimaOrders.of(nspec, curSpec.getD(), curSpec.getBd());
            return rspec;
        }
    }

    /**
     *
     */
    public static class RegArmaBic implements Comparable<RegArmaBic> {

        //private final RegArimaEstimation<SarimaModel> m_est;
        private final double bic;
        private final SarimaModel arima;

        /**
         *
         * @param data
         * @param spec
         * @param eps
         */
        RegArmaBic(final DoubleSeq data, final SarmaOrders spec, double eps) {
            IRegArimaComputer processor = RegArimaUtility.processor(true, eps);
            RegArimaModel<SarimaModel> model = RegArimaModel.<SarimaModel>builder()
                    .y(data)
                    .arima(SarimaModel.builder(spec).setDefault().build())
                    .build();
            RegArimaEstimation<SarimaModel> est = processor.process(model, null);
            if (est != null) {
                bic = est.statistics().getBIC2();
                arima = est.getModel().arima();
            } else {
                bic = NO_BIC;
                arima = null;
            }
        }

        @Override
        public int compareTo(RegArmaBic o) {
            return Double.compare(bic, o.bic);
        }

        /**
         *
         * @return
         */
        double getBIC() {
            return bic;
        }

        /**
         *
         * @return
         */
        SarmaOrders getSpecification() {
            return arima.orders().doStationary();
        }

        static void mergeInto(RegArmaBic[] estimations, RegArmaBic[] models) {
            if (estimations == null) {
                return;
            }
            int gmod = models.length;
            int nmax = estimations.length;
            if (nmax > gmod) {
                nmax = gmod;
            }
            // insert the new specifications in the old one
            for (int i = 0, icur = 0; i < nmax && icur < gmod; ++i) {
                double bic = estimations[i].getBIC();
                for (int j = icur; j < gmod; ++j) {
                    if (models[j] == null) {
                        models[j] = estimations[i];
                        icur = j + 1;
                        break;
                    } else if (models[j].getSpecification().equals(estimations[i].getSpecification())) {
                        icur = j + 1;
                        break;
                    } else if (models[j].getBIC() > bic) {
                        for (int k = gmod - 1; k > j; --k) {
                            models[k] = models[k - 1];
                        }
                        models[j] = estimations[i];
                        icur = j + 1;
                        break;
                    }

                }
            }
        }

        public static RegArmaBic[] sort(final DoubleSeq data, final SarmaOrders[] specs, double eps) {
            List<RegArmaBic> all = new ArrayList<>();
            for (int i = 0; i < specs.length; ++i) {
                RegArmaBic cur = new RegArmaBic(data, specs[i], eps);
                if (cur.getBIC() != NO_BIC) {
                    all.add(cur);
                }
            }
            Collections.sort(all);
            return all.toArray(new RegArmaBic[all.size()]);
        }
    }

    private RegArmaBic[] estimations;
    private final int nmod;
    private final boolean balanced, mixed, wn;
    private final double eps;
    private final int maxP, maxQ, maxBp, maxBq;

    private ArmaModule(Builder builder) {
        this.nmod = builder.modelsCount;
        this.maxP = builder.maxP;
        this.maxQ = builder.maxQ;
        this.maxBp = builder.maxBp;
        this.maxBq = builder.maxBq;
        this.balanced = builder.balanced;
        this.mixed = builder.mixed;
        this.wn = builder.wn;
        this.eps = builder.eps;
    }

    /**
     *
     */
    public void clear() {
        estimations = null;
    }

    /**
     *
     * @return
     */
    public int getCount() {
        return estimations == null ? 0 : estimations.length;
    }

    /**
     *
     * @return
     */
    public RegArmaBic[] getPreferedModels() {
        return estimations;
    }

    /**
     *
     * @param data
     * @param d
     * @param bd
     * @return
     */
    public SarmaOrders select(DoubleSeq data, final int d, final int bd) {
        int idmax = nmod;
        while (estimations[idmax - 1].getBIC() == NO_BIC) {
            --idmax;
        }
        SarmaOrders spec = estimations[0].getSpecification();
        int nr1 = spec.getP() + spec.getQ(), ns1 = spec.getBp() + spec.getBq();
        int nrr1 = Math.abs(spec.getP() + d - spec.getQ());
        int nss1 = Math.abs(spec.getBp() + bd - spec.getBq());
        double bmax = estimations[idmax - 1].getBIC() - estimations[0].getBIC();
        if (bmax < 0.003) {
            bmax = 0.0625;
        } else if (bmax < 0.03) {
            bmax = .25;
        } else {
            bmax = 1;
        }
        double vc11 = 0.01 * bmax;
        double vc2 = 0.0025 * bmax;
        double vc22 = 0.0075 * bmax;

        int idpref = 0;
        int icmod = 0;
        for (int i = 1; i < idmax; ++i) {
            SarmaOrders cur = estimations[i].getSpecification();
            int nr2 = cur.getP() + cur.getQ(), ns2 = cur.getBp() + cur.getBq();
            int nrr2 = Math.abs(cur.getP() + d - cur.getQ());
            int nss2 = Math.abs(cur.getBp() + bd - cur.getBq());
            double dbic = estimations[i].getBIC() - estimations[idpref].getBIC();
            int chk = 0;
            if ((nrr2 < nrr1 || nss2 < nss1) && nr1 == nr2 && ns1 == ns2 && dbic <= vc11 && balanced) {
                chk = 1;
            } else if (nrr2 < nrr1 && nr2 <= nr1 && ns2 == ns1 && dbic <= vc2
                    && cur.getP() > 0 && cur.getQ() > 0 && balanced) {
                chk = 2;
            } else if (((nrr2 == 0 && nrr2 < nrr1 && d > 0) || (nss2 == 0
                    && nss2 < nss1 && bd > 0))
                    && nr1 == nr2 && ns1 == ns2 && dbic <= vc11 && balanced) {
                chk = 3;
            } else if (nrr2 == 0 && nss2 == 0 && dbic < vc2 && balanced) {
                chk = 4;
            } else if (nr2 > nr1 && nrr2 == 0 && ns2 == ns1 && dbic < vc2 && balanced) {
                chk = 5;
            } else if (ns2 > ns1 && nss2 == 0 && nr2 == nr1 && dbic < vc2 && balanced) {
                chk = 6;
            } else if (ns2 < ns1 && ns2 > 0 && nr2 == nr1 && nss2 == 0 && dbic < vc2 && balanced) {
                chk = 7;
            } else if (i == 1 && nr1 == 0 && nr2 == 1 && ns2 == ns1 && dbic < vc2) {
                chk = 8;
            } else if (nr2 < nr1 && nr2 > 0 && ns2 == ns1 && dbic < vc2) {
                chk = 9;
            } else if (ns2 < ns1 && ns2 > 0 && nr2 == nr1 && dbic < vc2) {
                chk = 10;
            } else if (cur.getP() < spec.getP() && cur.getQ() == spec.getQ()
                    && nr2 > 0 && ns2 == ns1 && dbic < vc22) {
                chk = 11;
            }
            if (chk > 0) {
                ++icmod;
                double dc = estimations[i].getBIC() - estimations[0].getBIC();
                vc11 -= dc;
                vc2 -= dc;
                vc22 -= dc;
                nr1 = nr2;
                ns1 = ns2;
                nrr1 = nrr2;
                nss1 = nss2;
                idpref = i;
                spec = cur.clone();
            }
        }
        if (spec.getParametersCount() == 0) {
            if (idpref < nmod - 1) {
                return estimations[idpref + 1].getSpecification().clone();
            }
        }

        return estimations[idpref].getSpecification().clone();

    }

    /**
     *
     * @param data
     * @param freq
     * @param d
     * @param bd
     * @return
     */
    public SarmaOrders select(final DoubleSeq data, final int freq, final int d, final int bd) {
        clear();
        // step I

        SarmaOrders spec = new SarmaOrders(freq);
        SarmaOrders cur = null;

        estimations = new RegArmaBic[nmod];

        if (freq == 2)
            spec.setP(1);
        else
        spec.setP(3);
        spec.setQ(0);

        int nmax = 0;
        List<SarmaOrders> lspecs0 = new ArrayList<>();
        if (freq != 1) {
            for (int bp = 0, i = 0; bp <= maxBp; ++bp) {
                for (int bq = 0; bq <= maxBq; ++bq) {
                    if (mixed || (bp == 0 || bq == 0)) {
                        spec.setBp(bp);
                        spec.setBq(bq);
                        lspecs0.add(spec.clone());
                    }
                }
            }
            SarmaOrders[] specs0 = lspecs0.toArray(new SarmaOrders[lspecs0.size()]);
            RegArmaBic[] bic0 = RegArmaBic.sort(data, specs0, eps);
            if (bic0.length == 0) {
                return null;
            }
            cur = bic0[0].getSpecification().clone();
        } else {
            cur = spec.clone();
        }

        List<SarmaOrders> lspecs1 = new ArrayList<>();
        for (int p = 0, i = 0; p <= maxP; ++p) {
            for (int q = 0; q <= maxQ; ++q) {
                if (mixed || (p == 0 || q == 0)) {
                    cur.setP(p);
                    cur.setQ(q);
                    lspecs1.add(cur.clone());
                }
            }
        }
        SarmaOrders[] specs1 = lspecs1.toArray(new SarmaOrders[lspecs1.size()]);
        RegArmaBic[] bic1 = RegArmaBic.sort(data, specs1, eps);
        if (bic1.length == 0) {
            return null;
        }

        cur = bic1[0].getSpecification().clone();
        RegArmaBic.mergeInto(bic1, estimations);

        int spmax = maxBp, sqmax = maxBq;
        if (bd == 1) {
            spmax = 0;
        }
        if (freq != 1) {
            List<SarmaOrders> lspecs2 = new ArrayList<>();
            for (int bp = 0, i = 0; bp <= spmax; ++bp) {
                for (int bq = 0; bq <= sqmax; ++bq) {
                    if (mixed || (bp == 0 || bq == 0)) {
                        cur.setBp(bp);
                        cur.setBq(bq);
                        lspecs2.add(cur.clone());
                    }
                }
            }
            SarmaOrders[] specs2 = lspecs2.toArray(new SarmaOrders[lspecs2.size()]);
            RegArmaBic[] bic2 = RegArmaBic.sort(data, specs2, eps);
            if (bic2.length == 0) {
                return null;
            }
            RegArmaBic.mergeInto(bic2, estimations);
        }
        if (freq == 1) {
            if (estimations[0].getSpecification().getParametersCount() == 0) {
                return estimations[1].getSpecification().clone();
            } else {
                return estimations[0].getSpecification().clone();
            }
        } else {
            return select(data, d, bd);
        }
    }
}
