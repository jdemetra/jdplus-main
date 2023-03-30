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

/**
 *
 * @author PALATEJ
 * @param <S>
 * @param <R>
 */
public interface TsDocument<S extends ProcSpecification, R extends Explorable> extends ProcDocument<S, Ts, R> {

    @Override
    Ts getInput();

    @Override
    void set(S spec, Ts input);

    @Override
    void set(Ts input);

    void setAll(S spec, Ts input, R result);

    @Override
    R getResult();

    default ProcessingLog getLog() {
        if (getStatus() != ProcessingStatus.Valid || !(getResult() instanceof HasLog)) {
            return ProcessingLog.dummy();
        } else {
            return ((HasLog) getResult()).getLog();
        }
    }
}
