/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.toolkit.base.api.timeseries;

import jdplus.toolkit.base.api.information.Explorable;
import jdplus.toolkit.base.api.processing.HasLog;
import jdplus.toolkit.base.api.processing.ProcDocument;
import jdplus.toolkit.base.api.processing.ProcSpecification;
import jdplus.toolkit.base.api.processing.ProcessingLog;
import jdplus.toolkit.base.api.processing.ProcessingStatus;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PALATEJ
 * @param <S>
 * @param <R>
 */
public interface MultiTsDocument<S extends ProcSpecification, R extends Explorable> extends ProcDocument<S, List<Ts>, R> {

    @Override
    List<Ts> getInput();

    @Override
    void set(S spec, List<Ts> input);

    @Override
    void set(List<Ts> input);

    void setAll(S spec, List<Ts> input, R result);

    @Override
    R getResult();

    default ProcessingLog getLog() {
        if (getStatus() != ProcessingStatus.Valid || !(getResult() instanceof HasLog)) {
            return ProcessingLog.dummy();
        } else {
            return ((HasLog) getResult()).getLog();
        }
    }

    default boolean isTsFrozen() {
        List<Ts> ts = getInput();
        if (ts.isEmpty()) {
            return false;
        }
        for (Ts s : ts) {
            if (s.isFrozen()) {
                return true;
            }
        }
        return false;
    }

    default void freezeTs() {
        List<Ts> ts = getInput();
        if (ts.isEmpty()) {
            return;
        }
        boolean changed = false;
        List<Ts> nts=new ArrayList<>();
        for (Ts s:ts) {
            if (!s.isFrozen()) {
                nts.add(s.freeze());
                changed=true;
            }else{
                nts.add(s);
            }
        }
        if (changed) {
            set(nts);
        }
    }

    default void unfreezeTs() {
        List<Ts> ts = getInput();
        if (ts.isEmpty()) {
            return;
        }
        boolean changed = false;
        List<Ts> nts=new ArrayList<>();
        for (Ts s : ts){
            if (s.isFrozen()) {
                nts.add(s.unfreeze(TsFactory.getDefault(), TsInformationType.Data));
                changed = true;
            }else{
                nts.add(s);
            }
        }
        if (changed) {
            set(nts);
            getMetadata().put(TsFactory.DATE, LocalDate.now(Clock.systemDefaultZone()).format(DateTimeFormatter.ISO_DATE));
        }
    }

}
