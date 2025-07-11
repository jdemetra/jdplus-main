package _demo;

import com.formdev.flatlaf.FlatLightLaf;
import ec.util.various.swing.BasicSwingLauncher;
import jdplus.toolkit.base.api.timeseries.TsCollection;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.desktop.plugin.components.JTsGrid;
import jdplus.toolkit.desktop.plugin.ui.DemoTsBuilder;
import nbbrd.design.Demo;

import java.util.List;

public class JTsGridDemo {

    @Demo
    public static void main(String[] args) {
        FlatLightLaf.setup();
        new BasicSwingLauncher()
                .lookAndFeel(FlatLightLaf.class.getName())
                .content(JTsGridDemo::newDemoGrid)
                .launch();
    }

    private static JTsGrid newDemoGrid() {
        JTsGrid result = new JTsGrid();
        DemoTsBuilder tsBuilder = new DemoTsBuilder();
        result.setTsCollection(TsCollection.of(List.of(
                tsBuilder.name("Monthly TS").start(TsPeriod.parse("2010-01/P1M")).obsCount(24).build(),
                tsBuilder.name("Yearly TS").start(TsPeriod.parse("2010/P1Y")).obsCount(2).build()
        )));
        return result;
    }
}
