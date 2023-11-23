package tck.demetra.data;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import lombok.NonNull;

import java.util.List;
import java.util.function.Function;
import java.util.function.IntToDoubleFunction;
import java.util.stream.Stream;

@lombok.Value
@lombok.Builder
public class DataGenerator {

    public static final DataGenerator BY_INDEX = DataGenerator.builder().build();

    @lombok.Builder.Default
    @NonNull Function<TsDomain, IntToDoubleFunction> valueFactory = DataGenerator::indexPlusStartMonth;

    public @NonNull List<TsData> listOfTsData(@NonNull String... domains) {
        return Stream.of(domains).map(this::tsData).toList();
    }

    public @NonNull List<TsData> listOfTsData(@NonNull TsDomain... domains) {
        return Stream.of(domains).map(this::tsData).toList();
    }

    public @NonNull TsData tsData(@NonNull String domain) {
        return tsData(TsDomain.parse(domain));
    }

    public @NonNull TsData tsData(@NonNull TsDomain domain) {
        return TsData.of(domain.getStartPeriod(), DoubleSeq.onMapping(domain.getLength(), valueFactory.apply(domain)));
    }

    private static IntToDoubleFunction indexPlusStartMonth(TsDomain domain) {
        return i -> (i + domain.getStartPeriod().start().getMonthValue()) * 1.1;
    }
}
