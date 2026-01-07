package jdplus.toolkit.desktop.plugin.jfreechart;

import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.desktop.plugin.ui.DemoTsBuilder;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TsXYDatasetTest {

    @Test
    public void test() {
        assertThat(TsXYDataset.ofTs(List.of()).getSeriesCount())
                .isEqualTo(0);

        var ts = new DemoTsBuilder().name("Monthly TS").start(TsPeriod.parse("2010-01/P1M")).obsCount(24).build();

        assertThat(TsXYDataset.ofTs(List.of(ts)))
                .returns(1, TsXYDataset::getSeriesCount)
                .returns(ts.getMoniker().toString(), ds -> ds.getSeriesKey(0))
                .returns(ts.getData().getValues().size(), ds -> ds.getItemCount(0))
                .satisfies(ds -> {
                    for (int i = 0; i < ds.getSeriesCount(); i++) {
                        assertThat(ts.getData().getValues().get(i))
                                .isEqualTo(ds.getYValue(0, i))
                                .isEqualTo(ds.getY(0, i))
                                .isEqualTo(ds.getStartY(0, i))
                                .isEqualTo(ds.getEndY(0, i))
                                .isEqualTo(ds.getStartYValue(0, i))
                                .isEqualTo(ds.getEndYValue(0, i));
                        assertThat((double) ts.getData().getDomain().get(i).start().atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli())
                                .isEqualTo(ds.getXValue(0, i))
                                .isEqualTo(ds.getX(0, i))
                                .isEqualTo(ds.getStartX(0, i))
                                .isEqualTo(ds.getStartXValue(0, i));
                        assertThat((double) ts.getData().getDomain().get(i).end().atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli())
                                .isEqualTo(ds.getEndXValue(0, i))
                                .isEqualTo(ds.getEndX(0, i));
                    }
                })
        ;
    }
}