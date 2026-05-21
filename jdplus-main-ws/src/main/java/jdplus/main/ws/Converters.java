package jdplus.main.ws;

import jdplus.toolkit.base.api.data.AggregationType;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDataTable;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import jdplus.toolkit.base.api.timeseries.util.ObsGathering;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

class Converters {

    public static ToolkitMessages.Frequency fromTsUnit(TsUnit unit) {
        if (unit.equals(TsUnit.UNDEFINED)) {
            return ToolkitMessages.Frequency.FREQ_UNDEFINED;
        }
        switch (unit.getChronoUnit()) {
            case YEARS:
                if (unit.getAmount() == 1) {
                    return ToolkitMessages.Frequency.FREQ_YEARLY;
                }
                break;
            case MONTHS:
                if (unit.getAmount() == 6) {
                    return ToolkitMessages.Frequency.FREQ_HALF_YEARLY;
                }
                if (unit.getAmount() == 4) {
                    return ToolkitMessages.Frequency.FREQ_QUADRI_MONTHLY;
                }
                if (unit.getAmount() == 3) {
                    return ToolkitMessages.Frequency.FREQ_QUARTERLY;
                }
                if (unit.getAmount() == 2) {
                    return ToolkitMessages.Frequency.FREQ_BI_MONTHLY;
                }
                if (unit.getAmount() == 1) {
                    return ToolkitMessages.Frequency.FREQ_MONTHLY;
                }
                break;
            case DAYS:
                if (unit.getAmount() == 1) {
                    return ToolkitMessages.Frequency.FREQ_DAILY;
                }
                break;
        }
        throw new IllegalArgumentException("Unsupported unit " + unit);
    }

    public static TsUnit toTsUnit(ToolkitMessages.Frequency value) {
        return switch (value) {
            case FREQ_YEARLY -> TsUnit.P1Y;
            case FREQ_HALF_YEARLY -> TsUnit.P6M;
            case FREQ_QUADRI_MONTHLY -> TsUnit.P4M;
            case FREQ_QUARTERLY -> TsUnit.P3M;
            case FREQ_BI_MONTHLY -> TsUnit.P2M;
            case FREQ_MONTHLY -> TsUnit.P1M;
            case FREQ_UNDEFINED -> TsUnit.UNDEFINED;
            case FREQ_DAILY -> TsUnit.P1D;
            default -> throw new RuntimeException("Unreachable");
        };
    }

    public static ToolkitMessages.TsPeriod fromTsPeriod(TsPeriod value) {
        return ToolkitMessages.TsPeriod
                .newBuilder()
                .setFrequency(fromTsUnit(value.getUnit()))
                .setYear(value.year())
                .setPos(value.annualPosition())
                .build();
    }

    public static TsPeriod toTsPeriod(ToolkitMessages.TsPeriod value) {
        TsUnit unit = toTsUnit(value.getFrequency());
        return TsPeriod.of(
                unit,
                LocalDate.of(value.getYear(), 1, 1).plus(value.getPos() * unit.getAmount(), unit.getChronoUnit())
        );
    }

    private static List<Double> fromValues(DoubleSeq value) {
        return value.stream().boxed().toList();
    }

    private static DoubleSeq toValues(List<Double> value) {
        return DoubleSeq.onMapping(value.size(), value::get);
    }

    public static ToolkitMessages.TsData fromTsData(TsData value) {
        return ToolkitMessages.TsData
                .newBuilder()
                .setStart(fromTsPeriod(value.getStart()))
                .addAllValues(fromValues(value.getValues()))
                .build();
    }

    public static TsData toTsData(ToolkitMessages.TsData value) {
        return TsData.of(
                toTsPeriod(value.getStart()),
                toValues(value.getValuesList())
        );
    }

    public static ToolkitMessages.AggregationType fromAggregationType(AggregationType value) {
        return switch (value) {
            case None -> ToolkitMessages.AggregationType.AGGREGATION_NONE;
            case Sum -> ToolkitMessages.AggregationType.AGGREGATION_SUM;
            case Average -> ToolkitMessages.AggregationType.AGGREGATION_AVERAGE;
            case First -> ToolkitMessages.AggregationType.AGGREGATION_FIRST;
            case Last -> ToolkitMessages.AggregationType.AGGREGATION_LAST;
            case Max -> ToolkitMessages.AggregationType.AGGREGATION_MAX;
            case Min -> ToolkitMessages.AggregationType.AGGREGATION_MIN;
            default -> throw new IllegalArgumentException(value.name());
        };
    }

    public static AggregationType toAggregationType(ToolkitMessages.AggregationType value) {
        return switch (value) {
            case AGGREGATION_NONE -> AggregationType.None;
            case AGGREGATION_SUM -> AggregationType.Sum;
            case AGGREGATION_AVERAGE -> AggregationType.Average;
            case AGGREGATION_FIRST -> AggregationType.First;
            case AGGREGATION_LAST -> AggregationType.Last;
            case AGGREGATION_MAX -> AggregationType.Max;
            case AGGREGATION_MIN -> AggregationType.Min;
            default -> throw new IllegalArgumentException(value.name());
        };
    }

    public static ToolkitMessages.ObsGathering fromObsGathering(ObsGathering value) {
        return ToolkitMessages.ObsGathering
                .newBuilder()
                .setFrequency(fromTsUnit(value.getUnit()))
                .setAggregationType(fromAggregationType(value.getAggregationType()))
                .setAllowPartialAggregation(value.isAllowPartialAggregation())
                .setIncludeMissingValues(value.isIncludeMissingValues())
                .build();
    }

    public static ObsGathering toObsGathering(ToolkitMessages.ObsGathering value) {
        return ObsGathering
                .builder()
                .unit(toTsUnit(value.getFrequency()))
                .aggregationType(toAggregationType(value.getAggregationType()))
                .allowPartialAggregation(value.getAllowPartialAggregation())
                .includeMissingValues(value.getIncludeMissingValues())
                .build();
    }

    public static ToolkitMessages.Date fromLocalDate(LocalDate value) {
        return ToolkitMessages.Date
                .newBuilder()
                .setYear(value.getYear())
                .setMonth(value.getMonthValue())
                .setDay(value.getDayOfMonth())
                .build();
    }

    public static LocalDate toLocalDate(ToolkitMessages.Date value) {
        return LocalDate.of(value.getYear(), value.getMonth(), value.getDay());
    }

    public static ToolkitMessages.DistributionType fromDistributionType(TsDataTable.DistributionType value) {
        return switch (value) {
            case FIRST -> ToolkitMessages.DistributionType.DIST_FIRST;
            case LAST -> ToolkitMessages.DistributionType.DIST_LAST;
            case MIDDLE -> ToolkitMessages.DistributionType.DIST_MIDDLE;
        };
    }

    public static TsDataTable.DistributionType toDistributionType(ToolkitMessages.DistributionType value) {
        return switch (value) {
            case DIST_FIRST -> TsDataTable.DistributionType.FIRST;
            case DIST_LAST -> TsDataTable.DistributionType.LAST;
            case DIST_MIDDLE -> TsDataTable.DistributionType.MIDDLE;
            default -> throw new IllegalArgumentException(value.name());
        };
    }

    public static ToolkitMessages.ValueStatus fromValueStatus(TsDataTable.ValueStatus value) {
        return switch (value) {
            case PRESENT -> ToolkitMessages.ValueStatus.VS_PRESENT;
            case UNUSED -> ToolkitMessages.ValueStatus.VS_UNUSED;
            case BEFORE -> ToolkitMessages.ValueStatus.VS_BEFORE;
            case AFTER -> ToolkitMessages.ValueStatus.VS_AFTER;
            case EMPTY -> ToolkitMessages.ValueStatus.VS_EMPTY;
        };
    }

    public static TsDataTable.ValueStatus toValueStatus(ToolkitMessages.ValueStatus value) {
        return switch (value) {
            case VS_PRESENT -> TsDataTable.ValueStatus.PRESENT;
            case VS_UNUSED -> TsDataTable.ValueStatus.UNUSED;
            case VS_BEFORE -> TsDataTable.ValueStatus.BEFORE;
            case VS_AFTER -> TsDataTable.ValueStatus.AFTER;
            case VS_EMPTY -> TsDataTable.ValueStatus.EMPTY;
            default -> throw new IllegalArgumentException(value.name());
        };
    }

    public static ToolkitMessages.Matrix fromMatrix(jdplus.toolkit.base.api.math.matrices.Matrix value) {
        ToolkitMessages.Matrix.Builder result = ToolkitMessages.Matrix
                .newBuilder()
                .setNrows(value.getRowsCount())
                .setNcols(value.getColumnsCount());
        Arrays.stream(value.toArray()).forEach(result::addValues);
        return result.build();
    }
}
