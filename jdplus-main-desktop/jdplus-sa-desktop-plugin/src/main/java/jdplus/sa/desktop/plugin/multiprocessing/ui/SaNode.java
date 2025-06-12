/*
 * Copyright 2023 National Bank of Belgium
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
package jdplus.sa.desktop.plugin.multiprocessing.ui;

import jdplus.sa.base.api.EstimationPolicyType;
import jdplus.sa.base.api.SaDefinition;
import jdplus.sa.base.api.SaEstimation;
import jdplus.sa.base.api.SaItem;
import jdplus.sa.base.api.SaManager;
import jdplus.sa.base.api.SaProcessingFactory;
import jdplus.sa.base.api.SaSpecification;
import jdplus.toolkit.base.api.timeseries.Ts;
import jdplus.toolkit.base.api.timeseries.TsFactory;
import jdplus.toolkit.base.api.timeseries.TsInformationType;
import jdplus.toolkit.base.api.timeseries.TsMoniker;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author PALATEJ
 */
@lombok.Data
public class SaNode {

    /**
     * Status of the processing
     */
    public static enum Status {

        Unprocessed,
        NoData,
        Pending,
        Valid,
        Invalid;

        public boolean isError() {
            return this == NoData || this == Invalid;
        }

        public boolean isProcessed() {
            return this != Unprocessed && this != Pending;
        }
    }

    final int id;
    final TsMoniker moniker;
    final SaSpecification spec;

    volatile SaItem output;
    volatile Status status = Status.Unprocessed;

    public static SaNode of(int id, Ts ts, SaSpecification spec) {
        SaNode node = new SaNode(id, ts.getMoniker(), spec);
        if (ts.getType().encompass(TsInformationType.Data)) {
            node.setOutput(SaItem.of(ts, spec));
            node.status = Status.Unprocessed;
        }
        return node;
    }

    private static Status status(SaItem item) {
        SaEstimation estimation = item.getEstimation();
        if (estimation == null) {
            return Status.Unprocessed;
        }
        if (estimation.getResults() != null) {
            if (estimation.getResults().isValid()) {
                return Status.Valid;
            } else {
                return Status.Invalid;
            }
        }
        return Status.Unprocessed; // Invalid should be captured elsewhere
    }

    public static SaNode of(int id, SaItem item) {
        SaDefinition definition = item.getDefinition();
        SaNode node = new SaNode(id, definition.getTs().getMoniker(), definition.activeSpecification());
        node.output = item;
        node.status = status(item);
        return node;
    }

    void prepare() {
        synchronized (moniker) {
            if (output == null) {
                Ts ts = TsFactory.getDefault().makeTs(moniker, TsInformationType.Data);
                output = SaItem.of(ts, spec);
                status = Status.Unprocessed;
            }
        }
    }

    public void update(SaItem noutput) {
        synchronized (moniker) {
            output = noutput;
            if (noutput == null || noutput.getEstimation() == null || noutput.getEstimation().getResults() == null) {
                status = Status.Unprocessed;
            } else {
                status = output.getEstimation().getResults().isValid() ? Status.Valid : Status.Invalid;
            }
        }
    }

    public void process(ModellingContext context, boolean verbose) {
        if (status.isProcessed()) {
            return;
        }
        synchronized (moniker) {
            if (output == null) {
                Ts ts = TsFactory.getDefault().makeTs(moniker, TsInformationType.Data);
                output = SaItem.of(ts, spec);
            }
            status = output.process(context, verbose) ? Status.Valid : Status.Invalid;
        }
    }

    /**
     * Remove current processing.Keep any other information
     * @return False if the item was unprocessed, true otherwise
     */
    public boolean resetProcessing() {
        synchronized (moniker) {
            if (status == Status.Unprocessed)
                return false;
            status = Status.Unprocessed;
            if (output != null){
                output=output.toBuilder()
                        .estimation(null)
                        .processed(false)
                        .build();
            }
            return true;
        }
    }

    public SaNode withDomainSpecification(SaSpecification nspec) {
        synchronized (moniker) {
            SaItem item = output;
            if (item == null) {
                SaNode node = new SaNode(id, moniker, nspec);
                return node;
            } else {
                SaItem nitem=item.withDomainSpecification(nspec);
                return SaNode.of(id, nitem);
            }
        }
    }

    public SaNode withEstimationSpecification(SaSpecification nspec) {
        synchronized (moniker) {
            SaItem item = output;
            if (item == null) {
                SaNode node = new SaNode(id, moniker, nspec);
                return node;
            } else {
                SaItem nitem=item.withSpecification(nspec);
                return SaNode.of(id, nitem);
            }
        }
    }

    public SaEstimation results() {
        synchronized (moniker) {
            return output == null ? null : output.getEstimation();
        }
    }

    public String getName() {
        synchronized (moniker) {
            return output != null ? output.getName() : "series-" + id;
        }
    }

    public void setName(String nname) {
        synchronized (moniker) {
            if (output != null) {
                output = output.withName(nname);
            }
        }
    }

    public boolean isProcessed() {
        synchronized (moniker) {
            return status.isProcessed();
        }
    }

    public boolean isFrozen() {
        if (output != null) {
            return output.getDefinition().getTs().isFrozen();
        } else {
            return false;
        }
    }

    public SaSpecification domainSpec() {
        if (output != null) {
            return output.getDefinition().getDomainSpec();
        } else {
            return spec;
        }
    }

    public static Collection<SaProcessingFactory> factoriesOf(SaNode[] nodes) {
        List<SaProcessingFactory> facs = new ArrayList<>();
        for (int i = 0; i < nodes.length; ++i) {
            SaProcessingFactory fac = SaManager.factoryFor(nodes[i].getSpec());
            if (!facs.contains(fac)) {
                facs.add(fac);
            }
        }
        return Collections.unmodifiableList(facs);
    }

}
