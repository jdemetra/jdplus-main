package internal.toolkit.base.api.timeseries.util;

import jdplus.toolkit.base.api.data.BaseSeq;
import jdplus.toolkit.base.api.data.BaseSeqCursor;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import lombok.NonNull;
import nbbrd.design.NonNegative;

import java.time.LocalDateTime;

public interface ObsSeq extends BaseSeq {

    void sortByPeriod();

    double getValueAt(int i);

    long getPeriodIdAt(int index, LocalDateTime epoch, TsUnit unit);

    default int getIntPeriodIdAt(int index, LocalDateTime epoch, TsUnit unit) throws ArithmeticException {
        return Math.toIntExact(getPeriodIdAt(index, epoch, unit));
    }

    default double[] getValues() {
        double[] safeArray = new double[size()];
        for (int i = 0; i < safeArray.length; i++) {
            safeArray[i] = getValueAt(i);
        }
        return safeArray;
    }

    default @NonNull BaseSeqCursor cursor() {
        return new BaseSeqCursor() {
            @Override
            public void moveTo(@NonNegative int index) {
            }

            @Override
            public void skip(@NonNegative int n) {
            }
        };
    }
}
