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
package jdplus.sa.base.api;

import jdplus.toolkit.base.api.processing.ProcQuality;
import jdplus.toolkit.base.api.timeseries.TimeSelector;
import jdplus.toolkit.base.api.timeseries.Ts;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsFactory;
import jdplus.toolkit.base.api.timeseries.TsInformationType;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.regression.ModellingContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PUBLIC)
public final class SaItem {

    public static final String COMMENT = "comment";

    @lombok.NonNull
    String name;

    @lombok.NonNull
    SaDefinition definition;

    @lombok.Singular("meta")
    @lombok.EqualsAndHashCode.Exclude
    Map<String, String> meta;

    /**
     * Operational. Importance of this estimation
     */
    @lombok.EqualsAndHashCode.Exclude
    int priority;

    public String getComment() {
        return meta.get(COMMENT);
    }

    public SaItem copy() {
        return toBuilder()
                .estimation(estimation == null ? null : estimation.flush())
                .processed(false)
                .build();
    }

    /**
     * All information available after processing. SA processors must be able to
     * generate full estimations starting from definitions
     */
    @lombok.experimental.NonFinal
    @lombok.EqualsAndHashCode.Exclude
    private volatile SaEstimation estimation;

    @lombok.experimental.NonFinal
    @lombok.EqualsAndHashCode.Exclude
    private volatile boolean processed;

    public static SaItem of(Ts s, SaSpecification spec) {
        if (!s.getType().encompass(TsInformationType.Data)) {
            throw new IllegalArgumentException();
        }
        return SaItem.builder()
                .name(s.getName())
                .definition(SaDefinition.builder()
                        .domainSpec(spec)
                        .ts(s)
                        .policy(EstimationPolicyType.None)
                        .build())
                .build();
    }

    public SaItem withPriority(int priority) {
        return new SaItem(name, definition, meta, priority, estimation, processed);
    }

    public SaItem withName(String name) {
        return new SaItem(name, definition, meta, priority, estimation, processed);
    }

    public SaItem withInformations(Map<String, String> info) {
        return new SaItem(name, definition, Collections.unmodifiableMap(info), priority, estimation, processed);
    }

    public SaItem withComment(String ncomment) {
        Map<String, String> info = new HashMap<>(meta);
        info.put(COMMENT, ncomment);
        return new SaItem(name, definition, info, priority, estimation, processed);
    }

    public SaItem withDomainSpecification(SaSpecification dspec) {
        SaDefinition ndef = SaDefinition.builder()
                .ts(definition.getTs())
                .domainSpec(dspec)
                .estimationSpec(definition.activeSpecification())
                .policy(EstimationPolicyType.None)
                .build();
        return new SaItem(name, ndef, meta, priority, estimation, processed);
    }

    /**
     * Keep the domain specification and use the new estimationspec
     *
     * @param espec Estimation spec
     * @return
     */
    public SaItem withSpecification(SaSpecification espec) {
        SaDefinition ndef = SaDefinition.builder()
                .ts(definition.getTs())
                .domainSpec(definition.getDomainSpec())
                .estimationSpec(espec)
                .policy(EstimationPolicyType.None)
                .build();
        return new SaItem(name, ndef, meta, priority, null, false);
     }

    /**
     * Keep the domain and the estimation specifications and put a new time
     * series
     *
     * @param ts
     * @return
     */
    public SaItem withTs(Ts ts) {
        return SaItem.builder()
                .name(name)
                .definition(SaDefinition.builder()
                        .ts(ts)
                        .domainSpec(definition.getDomainSpec())
                        .estimationSpec(definition.getEstimationSpec())
                        .policy(EstimationPolicyType.None)
                        .build())
                .build();
    }

    public SaItem withTsMetaData(Map<String, String> info) {
        Ts cur = definition.getTs();
        Ts ncur = cur.toBuilder().clearMeta()
                .meta(info)
                .build();
        SaDefinition ndef = definition.toBuilder()
                .ts(ncur)
                .build();
        return new SaItem(name, ndef, meta, priority, estimation, processed);
    }

    public void accept() {
        synchronized (this) {
            if (estimation == null) {
                return;
            }
            estimation = estimation.withQuality(ProcQuality.Accepted);
        }
    }

    public void resetQuality() {
        synchronized (this) {
            if (estimation == null) {
                return;
            }
            estimation = SaManager.resetQuality(estimation);
        }
    }

    /**
     * Process this item.The Processing is always executed, even if the item has
     * already been estimated. To avoid re-estimation, use getEstimation (which
     * is not verbose by default)
     *
     * @param context Context could be null (if unused)
     * @param verbose
     * @return
     */
    public boolean process(ModellingContext context, boolean verbose) {
        synchronized (this) {
            if (!processed) {
                estimation = SaManager.process(definition, context, verbose);
                processed = true;
            }
        }
        return estimation.getResults() != null && estimation.getResults().isValid();
    }

    public boolean compute(ModellingContext context, boolean verbose) {
        synchronized (this) {
            if (!processed) {
                if (estimation == null) {
                    estimation = SaManager.process(definition, context, verbose);
                } else {
                    // workaround against incomplete estimation
                    // Unoptimized solution
                    // SaSpecification pointSpec = estimation.getPointSpec();
                    //if (pointSpec == null)
                    SaSpecification pointSpec = definition.activeSpecification();
                    SaDefinition pdef = SaDefinition.builder()
                            .ts(definition.getTs())
                            .domainSpec(pointSpec)
                            .build();
                    SaEstimation nestimation = SaManager.process(pdef, context, verbose);
                    if (nestimation == null) {
                        return false;
                    }
                    estimation = nestimation.withQuality(estimation.getQuality());
                }
                processed = true;
            }
        }
        return estimation.getQuality() != ProcQuality.Undefined;
    }

    public boolean isProcessed() {
        return processed;
    }

    /**
     * Gets the current estimation (Processing should be controlled by
     * isProcessed).
     *
     * @return The current estimation
     */
    public SaEstimation getEstimation() {
        return estimation;
    }

    /**
     * Remove the results (useful in case of memory problems), but keep the
     * quality
     */
    public void flush() {
        SaEstimation e = estimation;
        if (e != null) {
            synchronized (this) {
                estimation = estimation.flush();
            }
        }
    }

    public SaDocument asDocument() {
        SaEstimation e = getEstimation();
        if (e == null) {
            return new SaDocument(name, definition.getTs(), definition.activeSpecification(),
                    null, null, ProcQuality.Undefined);
        } else {
            return new SaDocument(name, definition.getTs(), definition.activeSpecification(),
                    e.getResults(), e.getDiagnostics(), e.getQuality());
        }
    }

    public SaItem refresh(EstimationPolicy policy, TsInformationType type) {
        return refresh(policy, null, type);
    }

    public SaItem refresh(EstimationPolicy policy, SaSpecification dspec, TsInformationType type) {
        TsData oldData = definition.getTs().getData();
        Ts nts = type != TsInformationType.None ? definition.getTs().unfreeze(TsFactory.getDefault(), type) : definition.getTs();
        if (dspec == null) {
            dspec = definition.getDomainSpec();
        }
        if (estimation == null) {
            SaDefinition ndef = SaDefinition.builder()
                    .ts(nts)
                    .domainSpec(dspec)
                    .estimationSpec(definition.activeSpecification())
                    .policy(policy.getPolicy())
                    .build();
            return new SaItem(name, ndef, meta, priority, null, false);
        } else {
            SaSpecification pspec = estimation.getPointSpec();
            SaProcessingFactory fac = SaManager.factoryFor(dspec);
            SaSpecification espec = definition.activeSpecification();
            if (fac != null) {
                if (pspec != null) {
                    TsDomain frozenSpan = policy.getFrozenSpan();
                    if (frozenSpan == null) {
                        switch (policy.getPolicy()) {
                            case LastOutliers ->
                                frozenSpan = oldData.getDomain().select(TimeSelector.excluding(0, oldData.getAnnualFrequency()));
                            case Current -> {
                                TsPeriod end = oldData.getEnd();
                                TsPeriod nend = nts.getData().getEnd();
                                int n = end.until(nend);
                                if (n > 0) {
                                    frozenSpan = TsDomain.of(end, n);
                                }
                            }
                        }
                    }
                    espec = fac.refreshSpec(pspec, dspec, policy.getPolicy(), frozenSpan);
                } else {
                    espec = dspec;
                }
            }
            SaDefinition ndef = SaDefinition.builder()
                    .ts(nts)
                    .domainSpec(dspec)
                    .estimationSpec(espec)
                    .policy(policy.getPolicy())
                    .build();
            return new SaItem(name, ndef, meta, priority, null, false);
        }
    }

}
